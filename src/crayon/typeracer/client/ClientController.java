package crayon.typeracer.client;

import crayon.typeracer.FXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


public class ClientController extends FXController {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean startGame = false;

    @FXML
    private StackPane clientScreen;
    @FXML
    private VBox clientConfig;
    @FXML
    private TextField serverIp;
    @FXML
    private TextField serverPort;

    private VBox clientEnterName;
    private TextField username;

    private boolean listening;
    private Thread listenThread;

    public void connect() {
        // connect to specified ip and port
        System.out.println("Connecting to " + serverIp.getText() + ":" + serverPort.getText());
        try {
            this.client = new Socket(serverIp.getText(), Integer.parseInt(serverPort.getText()));
            in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            out = new PrintWriter(this.client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(e.hashCode());
        }

        switchToEnterName();
    }

    private void switchToEnterName() {
        // clear screen
        clientScreen.getChildren().remove(clientConfig);

        /// create VBox to contain all
        clientEnterName = new VBox();
        clientScreen.getChildren().add(clientEnterName);

        // add label for name
        clientEnterName.getChildren().add(new Label("Name"));

        // add text field for username
        username = new TextField();
        clientEnterName.getChildren().add(username);

        // add button for confirmation
        Button submitName = new Button("Submit");
        submitName.setOnAction(this::startGame);
        clientEnterName.getChildren().add(submitName);

        this.listening = false;
    }

    private void startGame(ActionEvent e) {
        startGame = true;

        String data = "username " + username.getText();
        System.out.println("Sending data: " + data);
        out.println(data);

        clientScreen.getChildren().remove(clientEnterName);

        // start to listen to update
        listening = true;
        listenThread = new Thread(() -> listenToUpdate());
        listenThread.start();
    }

    private void listenToUpdate() {
        while(this.listening) {
            try {
                parseData(in.readLine());

            } catch (SocketException e) {
                // disconnected
                System.out.println("Disconnected");
                this.listening = false;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void parseData(String data) {
        System.out.println("Received: " + data);
        String[] args = data.split(" ");
        if(args[0].equals("count")) {
            // show countdown
            Platform.runLater(() -> {
                clearScreen();
                clientScreen.getChildren().add(new Text(args[1]));
            });

        } else if(args[0].equals("reset")) {
            // clear screen
            Platform.runLater(() -> {
                switchToEnterName();
            });
        }
    }

    private void clearScreen() {
        clientScreen.getChildren().removeAll(clientScreen.getChildren());
    }

    @Override
    public void stop() {
        this.listening = false;
        if(client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

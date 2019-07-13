package crayon.typeracer.client;

import crayon.typeracer.FXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


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

    public void connect(ActionEvent actionEvent) {
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
        submitName.setOnAction((e) -> {
            startGame(e);
        });
        clientEnterName.getChildren().add(submitName);
    }

    private void startGame(ActionEvent e) {
        startGame = true;

        String data = "username " + username.getText();
        System.out.println("Sending data: " + data);
        out.println(data);

        clientScreen.getChildren().remove(clientEnterName);
    }

    private void listenUpdate() {
        System.out.println("Listening to update...");
    }
}

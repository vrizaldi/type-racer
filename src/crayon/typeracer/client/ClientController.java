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
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;


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

    private Text typed;
    private Text untyped;

    private boolean listening;
    private int id;
    private String challenge;
    private HashMap<Integer, Integer> progresses;

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
        clearScreen();

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
        new Thread(() -> listenToUpdate()).start();
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
        if(data == null) {
            try {
                if(in.read() == -1) handleDisconnect();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Received: " + data);
        String[] args = data.split(" ");
        if(args[0].equals("id")) {
            this.id = Integer.parseInt(args[1]);    // update id

        } else if(args[0].equals("count")) {
            // show countdown
            Platform.runLater(() -> {
                clearScreen();
                clientScreen.getChildren().add(new Text(args[1]));
            });

        } else if(args[0].equals("start")) {
            System.out.println("Starting game...");
            this.challenge = data.substring(args[0].length() + 1);  // the rest is the challenge
            System.out.println("Challenge: " + challenge);
            switchToGame();

        } else if(args[0].equals("init")) {
            initScoreboard(args);
            Platform.runLater(() -> {
                updateChallenge();
                updateScoreboard();
            });


        } else if(args[0].equals("reset")) {
            // clear screen
            Platform.runLater(() -> {
                switchToEnterName();
            });
        }
    }

    private void initScoreboard(String[] args) {
        for(int i = 1; i < args.length; i++) {
            System.out.println("parsing " + args[i]);
            progresses.put(Integer.valueOf(args[i]), 0);
        }

        System.out.println("Parsed scoreboard: ");
        progresses.forEach((id, progress) -> {
            System.out.println(id + " " + progress);
        });
    }


    // INGAME
    private void switchToGame() {
        progresses = new HashMap<>();
        Platform.runLater(() -> {
            clearScreen();

            TextFlow textFlow = new TextFlow();
            typed = new Text();
            untyped = new Text();
            textFlow.getChildren().addAll(typed, untyped);
            this.clientScreen.getChildren().add(textFlow);

            updateScoreboard();
            updateChallenge();
        });
    }

    private void handleDisconnect() {
        // handle disconnect
        // go back to main menu
        this.listening = false;
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.sceneController.switchTo("mainmenu");
    }

    private void clearScreen() {
        clientScreen.getChildren().removeAll(clientScreen.getChildren());
    }

    private void updateScoreboard() {
    }

    private void updateChallenge() {
        int progress = this.progresses.get(this.id);
        if(progress > 0) typed.setText(challenge.substring(0, progress));
        else typed.setText("");

        typed.setText(challenge.substring(progress));
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

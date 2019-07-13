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
import org.fxmisc.richtext.InlineCssTextArea;

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
    private HashMap<Integer, Player> players;

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

        } else if(args[0].equals("scoreboard")) {
            parseScoreboard(args);

        } else if(args[0].equals("reset")) {
            // clear screen
            Platform.runLater(() -> {
                switchToEnterName();
            });
        }
    }

    private void parseScoreboard(String[] args) {
        for(int i = 1; i < args.length; i = i + 2) {
            System.out.println("parsing " + args[i] + " " + args[i + 1]);
            if(players.containsKey(Integer.valueOf(args[i]))) {
                // players already exist in scoreboard
                players.get(Integer.valueOf(args[i])).updateProgress(args[i + 1]);
            } else {
                players.put(Integer.valueOf(args[i]), new Player(Integer.parseInt(args[i + 1])));
            }
        }

        System.out.println("Parsed scoreboard: ");
        players.forEach((key, player) -> {
            System.out.println(player.getId() + " " + player.getProgress());
        });
    }


    // INGAME
    private void switchToGame() {
        players = new HashMap<>();
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
        int progress = this.players.get(this.id).getProgress();
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

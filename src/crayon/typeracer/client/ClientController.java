package crayon.typeracer.client;

import crayon.typeracer.FXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
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

    private TextField username;

    private Text typed;
    private Text untyped;
    private Pane raceVisual;
    private ImageView pointer;

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
        VBox clientEnterName = new VBox();
        clientScreen.getChildren().add(clientEnterName);
        clientEnterName.setId("enter-name-screen");

        // add label for name
        clientEnterName.getChildren().add(new Label("Name"));

        // add text field for username
        username = new TextField();
        clientEnterName.getChildren().add(username);
        double prefWidth = Screen.getPrimary().getVisualBounds().getWidth() / 3;
        username.setMaxWidth(prefWidth);
        username.setMinWidth(prefWidth);
        username.setPrefWidth(prefWidth);

        // add button for confirmation
        Button submitName = new Button("Submit");
        submitName.setOnAction(this::startGame);
        clientEnterName.getChildren().add(submitName);

        this.listening = false;
    }

    private void startGame(ActionEvent e) {
        startGame = true;

        String data = "username " + username.getText();
        this.sendData(data);

        // move on to wait for instruction from server
        clearScreen();

        // create VBox to contain all
        VBox waitScreen = new VBox();
        waitScreen.setId("wait-screen-client");
        clientScreen.getChildren().add(waitScreen);

        waitScreen.getChildren().add(new Text("Please wait, " + username.getText()));

        Button backToEnterName = new Button("Change name");
        backToEnterName.setOnAction((actionEvent) -> {
            this.sendData("unidentify");
            this.switchToEnterName();
        });
        waitScreen.getChildren().add(backToEnterName);

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
                Text count = new Text(args[1]);
                count.getStyleClass().add("counter");
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
            });

        } else if(args[0].equals("update")) {
            // update scoreboard
            System.out.println("Updating player " + args[1] + " to progress " + args[2]);
            final double POSX = Screen.getPrimary().getBounds().getWidth() * Integer.parseInt(args[2])
                    / this.challenge.length();
            this.players.get(Integer.valueOf(args[1]))
                    .update(Integer.valueOf(args[2]), POSX);
            this.updateChallenge();

        } else if(args[0].equals("finish")) {
            // player finished
            System.out.println("Finished");
            Platform.runLater(() -> {
                clearScreen();
                this.clientScreen.getChildren().add(new Text("Finished #" + args[1]));
            });


        } else if(args[0].equals("reset")) {
            // clear screen
            Platform.runLater(() -> {
                switchToEnterName();
            });
        }
    }

    private void initScoreboard(String[] args) {
        Platform.runLater(() -> {
            final double DISTANCE = raceVisual.getPrefHeight() / (args.length - 1);
            for(int i = 1; i < args.length; i++) {
                System.out.println("parsing " + args[i]);
                Player newPlayer = new Player();
                players.put(Integer.valueOf(args[i]), newPlayer);
                ImageView p = Integer.parseInt(args[i]) == this.id ? pointer : null;
                raceVisual.getChildren().add(newPlayer.initSnail(0.5 * DISTANCE + (i - 1) * DISTANCE, p));

            }

            System.out.println("Parsed scoreboard: ");
            players.forEach((id, players) -> {
                System.out.println(id + " " + players.getProgress());
            });
        });

    }


    // INGAME
    private void switchToGame() {
        players = new HashMap<>();
        Platform.runLater(() -> {
            clearScreen();

            VBox gamescreen = new VBox();
            gamescreen.setId("game");
            this.clientScreen.getChildren().add(gamescreen);

            TextFlow textFlow = new TextFlow();
            textFlow.setId("totype");
            gamescreen.getChildren().add(textFlow);

            typed = new Text();
            typed.setFill(Color.RED);
            untyped = new Text();
            untyped.setFill(Color.BLACK);
            textFlow.getChildren().addAll(typed, untyped);

            // add snail racing screen
            raceVisual = new Pane();
            final double RACE_VISUAL_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight() * 2 / 3;
            raceVisual.setMinHeight(RACE_VISUAL_HEIGHT);
            raceVisual.setMaxHeight(RACE_VISUAL_HEIGHT);
            raceVisual.setPrefHeight(RACE_VISUAL_HEIGHT);
            gamescreen.getChildren().add(raceVisual);

            pointer = new ImageView(new Image("file:res/pointer.gif"));
            pointer.setScaleX(0.2); pointer.setScaleY(0.2);
            raceVisual.getChildren().add(pointer);

            this.sceneController.getScene().setOnKeyTyped(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    handleTyping(event);
                }
            });

            updateChallenge();
        });
    }

    private void handleTyping(KeyEvent e) {
        this.sendData("type " + e.getCharacter());
    }


    // UPDATE UI
    private void clearScreen() {
        clientScreen.getChildren().removeAll(clientScreen.getChildren());
    }

    private void updateChallenge() {
        Platform.runLater(() -> {
            int progress = this.players.get(this.id).getProgress();
            final int MID = 15;
            if(progress < MID) {
                typed.setText(challenge.substring(0, progress));
                int maxindex = MID * 2 > this.challenge.length() ? this.challenge.length() : MID * 2;
                untyped.setText(challenge.substring(progress, maxindex));

            // progress >= MID
            } else if(this.challenge.length() >= MID) {
                // print the few middle character
                typed.setText(challenge.substring(progress - MID, progress));
                int maxindex = progress + MID > this.challenge.length() ? this.challenge.length() : progress + MID;
                untyped.setText(challenge.substring(progress, maxindex));

            // challenge length < MID
            } else {
                // print to the end
                typed.setText(challenge.substring(0, progress));
                untyped.setText(challenge.substring(progress));
            }
        });
    }


    // COMMUNICATION WITH SERVER
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

    private void sendData(String data) {
        System.out.println("Sending: " + data);
        out.println(data);
    }


    // CALLED WHEN PROGRAM STOPPED
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

    public void backToMainMenu(ActionEvent actionEvent) {
        this.sceneController.switchTo("mainmenu");
    }
}

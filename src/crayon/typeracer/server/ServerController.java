package crayon.typeracer.server;

import crayon.typeracer.FXController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ServerController extends FXController {

    private ServerSocket serverSocket;
    private HashMap<Integer, PlayerListener> players = new HashMap<>();
    private int playerCount = 0;
    private boolean waitForPlayer;
    private boolean isIngame;
    private String challenge;       // the string to be typed

    @FXML
    StackPane serverScreen;
    @FXML
    TextField port;
    @FXML
    VBox serverConfig;

    VBox waitingScreen;
    TextArea dataView;
    Button gameToggle;

    // SERVERCONFIG SCREEN
    public void openConnection(ActionEvent e) {
        this.playerCount = 0;
        this.switchToWaitingRoom();
    }

    private void switchToWaitingRoom() {
        // open a connection
        try {
            System.out.println("Opening a connection...");
            serverSocket = new ServerSocket(Integer.parseInt(this.port.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // clear screen
        clearscreen();

        // create the waiting room screen
        // create VBox to hold all
        this.waitingScreen = new VBox();
        this.serverScreen.getChildren().add(this.waitingScreen);

        // create title
        this.waitingScreen.getChildren().add(new Text("SERVER"));

        // create playerList text area
        this.dataView = new TextArea();
        this.updatePlayerList();
        this.dataView.setEditable(false);
        this.dataView.setFocusTraversable(false);
        this.waitingScreen.getChildren().add(dataView);

        // create start game button
        gameToggle = new Button("Enter Challenge");
        gameToggle.setOnAction(this::enterChallenge);
        this.waitingScreen.getChildren().add(gameToggle);

        isIngame = false;

        // wait for players to connect
        ServerController controller = this;     // to be used inside anon class
        this.waitForPlayer = true;
        Runnable openCon = new Runnable() {
            @Override
            public void run() {
                // keep looking for player until closed
                while(waitForPlayer) {
                    try {
                        // attach PlayerListener to the connected player
                        System.out.println("Waiting for players...");
                        PlayerListener player = new PlayerListener(
                                serverSocket.accept(), playerCount, controller);

                        // run each PlayerListener on a single thread
                        Thread thread = new Thread(player);
                        thread.start();

                        players.put(playerCount++, player);

                        updatePlayerList();

                    } catch (IOException e) {
                       // finished looking for player
                        System.out.println("Stop waiting for players...");
                        waitForPlayer = false;
                    }
                }
            }
        };

        new Thread(openCon).start();
    }


    // WAITINGROOM SCREEN
    public void enterChallenge(ActionEvent actionEvent) {
        // switch to screen where server can enter challenge
        // stop waiting for player
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // allow textarea to be edited
        dataView.setEditable(true);
        dataView.setFocusTraversable(true);
        dataView.setText("THIS WILL BE THE TEXT TO BE TYPED");

        // edit the button
        gameToggle.setText("Start Game");
        gameToggle.setOnAction(this::startGame);
    }


    public void updatePlayerList() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String playerListTxt =
                    "Opened a connection on " + serverSocket.getLocalSocketAddress() + "\n";
                Object[] playerArr = players.values().toArray();
                playerListTxt += countIdentified(playerArr) + "/" + playerArr.length + " players identified:\n";
                for(int i = 0; i < playerArr.length; i++) {
                    PlayerListener player = (PlayerListener)playerArr[i];
                    if(player.getUsername().isEmpty())
                        playerListTxt += player.getClient().getRemoteSocketAddress() + " unidentified\n";
                    else
                        playerListTxt += player.getUsername() + " (" + player.getClient().getRemoteSocketAddress()
                            + ")\n";
                }
                dataView.setText(playerListTxt);
            }
        });

    }

    private int countIdentified(Object[] playerArr) {
        int count = 0;
        for(int i = 0; i < playerArr.length; i++) {
            if(((PlayerListener)playerArr[i]).getUsername().isEmpty()) continue;
            else count++;
        }
        return count;
    }


    // ENTER CHALLENGE SCREEN
    public void startGame(ActionEvent actionEvent) {
        // change start game to stop game on button
        gameToggle.setText("Stop Game");
        gameToggle.setOnAction(this::stopGame);

        // save challenge
        this.challenge = dataView.getText();

        dataView.setText("Game started");
        isIngame = true;
        countDown(3);
    }

    // IN GAME
    private void stopGame(ActionEvent e) {
        isIngame = false;
        switchToWaitingRoom();
        broadcast("reset");
        players.forEach((key, player) -> {
            player.reset();
        });
    }

    private void countDown(int from) {
        if(isIngame) {
            if(from > 0) {
                // still counting down
                broadcast("count " + from);
                dataView.setText(String.valueOf(from));

                Timer counter = new Timer();
                counter.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        countDown(from - 1);
                    }
                }, 1000);

            } else {
                // start the game
                broadcast("start " + challenge);

                // show score on dataView
                updateScoreboard();
                initClientScoreboard();
            }
        }

    }

    private void updateScoreboard() {

        String scoreboardServer = "Current Scoreboard:\n";
        for(Integer key : players.keySet()) {
            PlayerListener player = players.get(key);
            scoreboardServer += player.getUsername() + "\t\t" + player.getProgress() + "/" + challenge.length() + "\n";
        }
        dataView.setText(scoreboardServer);
    }

    private void initClientScoreboard() {
        String scoreboardClient = "init ";
        for(Integer key : players.keySet()) {
            PlayerListener player = players.get(key);
            scoreboardClient += player.getId() + " ";  // player's id progress separated by space
        }
        broadcast(scoreboardClient);
    }


    // UTIL
    private void clearscreen() {
        serverScreen.getChildren().remove(serverScreen.getChildren());
    }


    // to communicate with client
    private void broadcast(String data) {
        // broadcast data to all players
        System.out.println("Broadcasting: " + data);
        players.forEach((key, player) -> {
            player.sendData(data);
        });
    }

    public void disconnect(int id) {
        this.players.remove(id);

        updatePlayerList();
    }


    // ON PROGRAM EXIT
    public void stop() {
        System.out.println("Exiting program...");
        try {
            if(serverSocket != null) serverSocket.close();
            this.players.forEach((key, player) -> {
                player.stop();
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(e.hashCode());
        }
    }
}

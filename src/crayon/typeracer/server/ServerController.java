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
    private int playerCount;
    private boolean waitForPlayer;
    private boolean isIngame;

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
    public void openConnection(ActionEvent actionEvent) {
        // open a connection
        try {
            System.out.println("Opening a connection...");
            serverSocket = new ServerSocket(Integer.parseInt(this.port.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }

         // clear screen
        System.out.println(serverScreen.getChildren().remove(serverConfig));

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
        gameToggle = new Button("Start Game");
        gameToggle.setOnAction((e) -> {
            startGame(e);
        });
        this.waitingScreen.getChildren().add(gameToggle);

        isIngame = false;

        // wait for players to connect
        this.playerCount = 0;
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
    public void startGame(ActionEvent actionEvent) {
        // stop waiting for player
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // change start game to stop game on button
        gameToggle.setText("Stop Game");

        dataView.setText("Game started");
        isIngame = true;
        countDown(3);

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


    // IN GAME
    private void countDown(int from) {
        if(isIngame) {
            if(from > 0) {
                // still counting down
                broadcast("count " + from);

                Timer counter = new Timer();
                counter.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        countDown(from - 1);
                    }
                }, 1000);

            } else {
                // start the game
                broadcast("clearscreen");
            }
        }

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
        try {
            if(serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(e.hashCode());
        }
    }
}

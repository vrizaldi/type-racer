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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ServerController extends FXController {

    private ServerSocket serverSocket;
    private HashMap<Integer, PlayerListener> players = new HashMap<>();
    private int playerCount;
    private boolean waitForPlayer;

    @FXML
    StackPane serverScreen;
    @FXML
    TextField port;
    @FXML
    VBox serverConfig;

    VBox waitingScreen;
    TextArea playerList;

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
        this.waitingScreen.getChildren().add(new Text("WAITINGROOM"));

        // create playerList text area
        String playerListTxt =
                "Opened a connection on " + this.serverSocket.getLocalSocketAddress();
        this.playerList = new TextArea(playerListTxt);
        this.playerList.setEditable(false);
        this.playerList.setFocusTraversable(false);
        this.waitingScreen.getChildren().add(playerList);

        // create start game button
        Button startGame = new Button("Start Game");
        startGame.setOnAction((e) -> {
            startGame(e);
        });
        this.waitingScreen.getChildren().add(startGame);

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

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                updatePlayerList();
                            }
                        });

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

        // switch to game
        this.sceneController.switchTo("server");
    }

    public void disconnect(int id) {
        this.players.remove(id);

        Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updatePlayerList();
                }
            });
    }

    public void updatePlayerList() {
        String playerListTxt =
                "Opened a connection on " + serverSocket.getLocalSocketAddress() + "\n";
            Object[] playerArr = players.values().toArray();
            for(int i = 0; i < playerArr.length; i++) {

                playerListTxt += ((PlayerListener)playerArr[i]).getClient().getRemoteSocketAddress() +" connected\n";
            }
            playerList.setText(playerListTxt);

    }


    public void stop() {
        try {
            if(serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(e.hashCode());
        }
    }
}

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

public class ServerController extends FXController {

    private ServerSocket serverSocket;
    private ArrayList<PlayerListener> players = new ArrayList<PlayerListener>();
    private int playerCount;

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
        Runnable openCon = new Runnable() {
            @Override
            public void run() {
                // keep looking for player until closed
                while(true) {
                    try {
                        // attach PlayerListener to the connected player
                        System.out.println("Waiting for players...");
                        PlayerListener player = new PlayerListener(
                                serverSocket.accept(), playerCount, controller);

                        // run each PlayerListener on a single thread
                        Thread thread = new Thread(player);
                        thread.start();

                        players.add(player);
                        playerCount++;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                             String playerListTxt =
                                    "Opened a connection on " + serverSocket.getLocalSocketAddress() + "\n";
                                for(int i = 0; i < playerCount; i++) {
                                    playerListTxt += players.get(i).getClient().getRemoteSocketAddress()
                                            +" connected\n";
                                }
                                playerList.setText(playerListTxt);
                            }
                        });

                    } catch (IOException e) {
                       // finished looking for player
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
}

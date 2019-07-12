package crayon.typeracer.server;

import crayon.typeracer.FXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerController extends FXController {

    ServerSocket serverSocket;

    @FXML
    TextField port;

    // SERVERCONFIG SCREEN
    public void openConnection(ActionEvent actionEvent) {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(port.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.sceneController.switchTo("waitingroom");
    }


    // WAITINGROOM SCREEN
    public void startGame(ActionEvent actionEvent) {
        this.sceneController.switchTo("server");
    }
}

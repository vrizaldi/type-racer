package crayon.typeracer.client;

import crayon.typeracer.FXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.Socket;


public class ClientController extends FXController {

    private Socket client;

    @FXML
    private VBox clientConfig;
    @FXML
    private TextField serverIp;
    @FXML
    private TextField serverPort;

    public void connect(ActionEvent actionEvent) {
        // connect to specified ip and port
        System.out.println("Connecting to " + serverIp.getText() + ":" + serverPort.getText());
        try {
            this.client = new Socket(serverIp.getText(), Integer.parseInt(serverPort.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

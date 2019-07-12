package crayon.typeracer.client;

import crayon.typeracer.FXController;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class ClientController extends FXController {

    public void showServerConfig(ActionEvent actionEvent) {
        // show server connection configuration
        this.sceneController.switchTo("serverconfig");
    }

    public void showClientConfig(ActionEvent actionEvent) {
        // show client connection configuration
        this.sceneController.switchTo("clientconfig");
    }

    public void exitGame(ActionEvent actionEvent) {
        // exit the game
        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();
    }
}

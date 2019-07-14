package crayon.typeracer;

import crayon.typeracer.FXController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

import javax.annotation.Resources;
import java.net.URL;

public class MainMenuController extends FXController {

    @FXML
    protected void initialize(URL location, Resources resources) {
    }

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

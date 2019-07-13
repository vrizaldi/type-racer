package crayon.typeracer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private SceneController sceneController;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(new Pane(), 300, 275);
        sceneController = new SceneController(scene);
        loadScreens(sceneController);
        sceneController.switchTo("mainmenu");   // load mainmenu

        primaryStage.setTitle("Type Racer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadScreens(SceneController sceneController) {
        // load all screens
        String[] screens = {"mainmenu", "serverconfig", "waitingroom", "server", "clientconfig", "client"};
        for(int i = 0; i < screens.length; i++) {
            sceneController.addScreen(screens[i],
                new FXMLLoader(getClass().getResource("screens/" + screens[i] + ".fxml")));
        }
    }

    @Override
    public void stop() {
        sceneController.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

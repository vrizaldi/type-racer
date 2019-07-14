package crayon.typeracer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private SceneController sceneController;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(new Pane(), 300, 275);
        sceneController = new SceneController(scene);
        loadScreens(sceneController);
        sceneController.switchTo("mainmenu");   // load mainmenu
        scene.getStylesheets().add(getClass().getResource("screens/style.css").toExternalForm());

        primaryStage.setTitle("Type Racer");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
        primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.isAltDown() && event.getCode() == KeyCode.F11) primaryStage.setFullScreen(true);
            }
        });
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

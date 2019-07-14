package crayon.typeracer;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class SceneController {

    private Scene main;
    private HashMap<String, Pane> roots;
    private ArrayList<FXController> controllers;
    private String currentRoot = "";

    SceneController(Scene main) {
        this.main = main;
        roots = new HashMap<String, Pane>();
        controllers = new ArrayList<>();
    }

    void addScreen(String name, FXMLLoader loader) {
        try {
            this.roots.put(name, loader.load());
            FXController controller = loader.getController();
            controller.setSceneController(this);
            controllers.add(controller);
            System.out.println("added " + name + " " + roots.get(name).toString());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    Pane deleteScreen(String name) {
        return this.roots.remove(name);
    }

    public void switchTo(String name) {
        if(this.roots.containsKey(name)) {
            this.main.setRoot(this.roots.get(name));
            this.currentRoot = name;
            System.out.println("Switch to screen " + name);
        }
        else System.out.println("Key " + name + " not found");
    }

    public String getCurrentRoot() {
        return this.currentRoot;
    }

    public Scene getScene() {
        return this.main;
    }

    public void stop() {
        this.controllers.forEach((controller) -> {
            controller.stop();
        });
    }
}

package crayon.typeracer;

import java.util.HashMap;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

class SceneController {

    private Scene main;
    private HashMap<String, Pane> roots;

    SceneController(Scene main) {
        this.main = main;
        roots = new HashMap<String, Pane>();
    }

    void addScreen(String name, Pane root) {
        this.roots.put(name, root);
    }

    Pane deleteScreen(String name) {
        return this.roots.remove(name);
    }

    void switchTo(String name) {
        this.main.setRoot(this.roots.get(name));
    }

}

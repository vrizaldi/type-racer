package crayon.typeracer.client;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Player {
    private int progress;
    private ImageView snail;

    public ImageView initSnail(double posY) {
        System.out.println("initialising snail at " + posY);
        snail = new ImageView(new Image("file:res/snail.gif"));
        snail.setScaleX(-0.5);
        snail.setScaleY(0.5);
        snail.relocate(0, posY);
        return snail;
    }

    public void update(int progress, double posX) {
        this.progress = progress;

        snail.setX(posX);
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public ImageView getSnail() {
        return this.snail;
    }
}

package crayon.typeracer.client;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Player {
    private int progress;
    private ImageView snail;
    private ImageView pointer;

    public ImageView initSnail(double posY, ImageView pointer) {
        System.out.println("initialising snail at " + posY);
        snail = new ImageView(new Image("file:res/snail.gif"));
        snail.setScaleX(-0.5);
        snail.setScaleY(0.5);
        snail.relocate(0, posY);
        this.pointer = pointer;
        if(pointer != null) pointer.relocate(60, posY);
        return snail;
    }

    public void update(int progress, double posX) {
        this.progress = progress;

        snail.setX(posX);
        if(pointer != null) pointer.setX(posX + 60);
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

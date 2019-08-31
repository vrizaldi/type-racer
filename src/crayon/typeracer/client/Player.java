package crayon.typeracer.client;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Player {
    private int progress;
    private int offset;
    private ImageView snail;
//    private ImageView pointer;

    public ImageView initSnail(double posY, boolean main) {
        System.out.println("initialising snail at " + posY);
        String url = "file:res/snail.gif";
        if(main) url = "file:res/snail main.gif";
        snail = new ImageView(new Image(url));
        float scale = 0.5f;
        offset = 0;
        snail.setScaleX(-scale);
        snail.setScaleY(scale);
        snail.relocate(0 + offset, posY + offset);
//        this.pointer = pointer;
//        if(pointer != null) pointer.relocate(60, posY);
        return snail;
    }

    public void update(int progress, double posX) {
        this.progress = progress;

        snail.setX(posX + offset);
 //       if(pointer != null) pointer.setX(posX + 60);
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

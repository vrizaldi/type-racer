package crayon.typeracer.client;

public class Player {
    private int id;
    private int progress;

    public Player(int progress) {
        this.progress = progress;
    }

    public void updateProgress(String progress) {
        this.progress = Integer.parseInt(progress);
    }

    public int getId() {return id;}
    public int getProgress() {return progress;}
}

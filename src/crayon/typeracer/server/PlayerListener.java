package crayon.typeracer.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/** Listens to a player in server
 *  each player has a PlayerListener attached
 */
public class PlayerListener implements Runnable {

    // SERVER USE
    private ServerController controller;
    private Socket client;
    private int id;
    private boolean connected;
    private BufferedReader in;
    private PrintWriter out;

    // INGAME STATE
    private String username;
    private float progress;

    public PlayerListener(@NotNull Socket client, int id, ServerController controller) {

        this.controller = controller;

        this.client = client;
        this.id = id;
        this.connected = true;

        try {
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new PrintWriter(client.getOutputStream(), true);

        } catch(IOException e) {
            System.out.println("Error getting IOStream for " + id);
            System.exit(e.hashCode());
        }

    }

    @Override
    public void run() {
        while(this.client.isClosed() == false) {
            try {
                parseData(this.in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.connected = false;
    }

    private void parseData(String data) {
        System.out.println(data);
    }

    public void sendData(String data) {
        out.println(data);
    }
}

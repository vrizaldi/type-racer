package crayon.typeracer.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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
    private int progress;

    public PlayerListener(@NotNull Socket client, int id, ServerController controller) {

        System.out.println("Player " + id + " connected from " + client.getRemoteSocketAddress());

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

        this.username = "";
        this.progress = 0;
    }

    @Override
    public void run() {
        while(this.connected) {
            try {
                parseData(this.in.readLine());

            } catch(SocketException e) {
                handleDisconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseData(String data) {
        if(data == null) {
            try {
                if(in.read() == -1) handleDisconnect();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Received: " + data);
        String[] args = data.split(" ");
        if(args[0].equalsIgnoreCase("username")) {
            // identify as given username
            // all strings after "username" are taken as name
            this.username = "";
            for(int i = 1; i < args.length; i++) {
                username += args[i] + " ";
            }
            this.username = this.username.trim();
            System.out.println(this.client.getRemoteSocketAddress() + " identified as " + this.username);
            controller.updatePlayerList();

            // reply with its id
            sendData("id " + this.id);
        }
    }

    private void handleDisconnect() {
        // player disconnected
        System.out.println("Player " + id + " disconnected");
        this.controller.disconnect(this.id);
        this.connected = false;
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {return id;}

    public void sendData(String data) {
        out.println(data);
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }


    public void incProgress() {
        this.progress++;
    }

    public int getProgress() {
        return this.progress;
    }

    public Socket getClient() {
        return client;
    }

    public void reset() {
        this.username = "";
        this.progress = 0;
    }

    public void stop() {
        this.connected = false;
        try {
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package Client;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends Thread {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader serverReader;
    private String response;

    public ClientThread (Socket socket) {
        try {
            //Connect
            this.socket = socket;
            this.serverReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        }
    }
    public void sendJSON (JsonObject jsonObject) {
        try {
            StringWriter stringWriter = new StringWriter();
            Json.createWriter(stringWriter).writeObject(jsonObject);
            this.printWriter = new PrintWriter(this.socket.getOutputStream(), true);
            this.printWriter.println(stringWriter);
        } catch (Exception e) {
            interrupt();
        }
    }
    public void run() {
        try {
            JsonObject json = Json.createReader(serverReader).readObject();
            if (json.containsKey("list") && json.getString("list").equals("success")) {
                if (json.containsKey("user")) {
                    response = json.getJsonArray("user").toString();
                }
                if (json.containsKey("file"))
                    response = json.getJsonArray("file").toString();
            } else if (json.containsKey("add") && json.getString("add").equals("success")) {
                if (json.containsKey("peer"))
                    response = json.get("peer").toString();
                if (json.containsKey("file"))
                    response = json.get("file").toString();
            } else {
                response = "-1";
            }
        } catch (Exception e) {
            interrupt();
        }
    }
    public String getResponse() {
        return response;
    }
}

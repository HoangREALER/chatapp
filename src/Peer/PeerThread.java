package Peer;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class PeerThread extends Thread {
    private PrintWriter printWriter;
    private String username;
    private Peer peer;
    private Socket socket;
    public PeerThread(Socket socket, String username, Peer peer) throws IOException {
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.username = username;
        this.peer = peer;
    }
    public void sendMessage(String message) {
        StringWriter stringWriter = new StringWriter();
        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                .add("username", username)
                .add("message", message)
                .build()
        );
        printWriter.println(stringWriter.toString());
    }

    public void sendFileRequest(String filename) throws IOException {
        StringWriter stringWriter = new StringWriter();
        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                .add("filename", filename)
                .add("request", "download")
                .build()
        );
        printWriter.println(stringWriter.toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        JsonObject file_response = Json.createReader(reader).readObject();
        if (file_response.containsKey("filename")  && file_response.containsKey("file") &&
                !file_response.getString("file").equals("fail")) {
            convertFile(file_response.getString("file"), file_response.getString("filename"));
        }
    }
    private void convertFile(String file_string, String file_name) {
        try {
            byte[] bytes = Base64.getDecoder().decode(file_string);
            File file = new File(this.peer.getFolder() + "/receive/" + file_name);
            FileOutputStream fop = new FileOutputStream(file, false);
            fop.write(bytes);
            fop.flush();
            fop.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void closeEverything() {
        try {
            if (socket != null)
                socket.close();
            if (printWriter != null)
                printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

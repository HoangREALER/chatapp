package Peer;

import ui.ChatGUI;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PeerServerThread extends Thread {
    private ServerSocket serverSocket;
    private Peer peer;
    private Set<PeerServerSubThread> subThreads = new HashSet<PeerServerSubThread>();
    private ChatGUI chatGUI;
    public PeerServerThread(String portNumb, Peer peer) throws IOException {
        serverSocket = new ServerSocket(Integer.valueOf(portNumb));
        this.peer = peer;
    }
    public void run() {
        try {
            while (true) {
                PeerServerSubThread subThread = new PeerServerSubThread(this, serverSocket.accept());
                subThreads.add(subThread);
                subThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void processJson(String users_msg, Socket socket) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(users_msg));
            JsonObject json = jsonReader.readObject();
            if (json.containsKey("message") && json.containsKey("username"))
                printMessage(json);
            else if (json.containsKey("filename") && json.containsKey("request") && json.getString("request").equals("download"))
                sendFile(json.getString("filename"), socket);
            else
                return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void printMessage(JsonObject json) {
        String username = json.getString("username");
        String message = json.getString("message");
        peer.chatGUI.updateChat("[" + username + "]: " + message);
    }
    private void sendFile(String filename, Socket socket) throws IOException {
        try {
            File file_transfer = new File(this.peer.getFolder() + "/" + filename);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            StringWriter stringWriter = new StringWriter();
            Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                    .add("filename", filename)
                    .add("file", convertFileToString(file_transfer))
                    .build()
            );
            printWriter.println(stringWriter.toString());
        } catch (Exception e) {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            StringWriter stringWriter = new StringWriter();
            Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                    .add("file", "fail")
                    .build()
            );
            printWriter.println(stringWriter.toString());
        }
    }
    private String convertFileToString(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(Base64.getEncoder().encode(bytes));
    }
    public Set<PeerServerSubThread> getSubThreads() {
        return this.subThreads;
    }

    public void removeSubThreads() {
        Iterator<PeerServerSubThread> i = this.subThreads.iterator();
        while(i.hasNext()){
            PeerServerSubThread subThread = i.next();
            i.remove();
            subThread.stopServerSubThread();
        }
    }
}

package Peer;

import Client.Client;
import ui.ChatGUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Peer {

    private final String host = "localhost";
    private final int host_port = 5000;
    public String username = null;
    public String peerPort = null;
    private int peer_id = -1;
    private String directoryPath;
    public Set<PeerThread> peerThreads = new HashSet<PeerThread>();
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private PeerServerThread peerServerThread;
    private Client client;
    public ChatGUI chatGUI;

    public Peer(String username, String port, String directoryPath) throws IOException {
        // set host and port
        this.username = username;
        this.peerPort = port;
        this.directoryPath = directoryPath;
        this.peerServerThread = new PeerServerThread(port, this);
        peerServerThread.start();
        this.client = new Client(this.host, this.host_port, this);
    }

    public String getFolder() {
        return this.directoryPath;
    }

    public void removeThreads() {
        Iterator<PeerThread> i = this.peerThreads.iterator();
        while (i.hasNext()) {
            PeerThread peerThread = i.next();
            i.remove();
            peerThread.closeEverything();
        }
    }

    public void sendMessage(String message) {
        try {
            peerThreads.forEach(peerThread -> {
                peerThread.sendMessage(message);
            });
        } catch (Exception e) {
            //Exception
            e.printStackTrace();
        }
    }

    public String clientPrintUsers() {
        return client.getUserList();
    }

    public int clientAddUser() {
        this.peer_id = Integer.parseInt(client.addUser(this.username, this.peerPort));
        return this.peer_id;
    }

    public String clientGetUser(String username) {
        return client.getUserInfo(username);
    }

    public String clientPrintFiles() {
        return client.getFileList();
    }

    public boolean addFiles() {
        File folder = new File(this.directoryPath);
        File[] listOfFiles = folder.listFiles();
        File file;
        boolean res = true;
        for (int i = 0; i < listOfFiles.length; i++) {
            file = listOfFiles[i];
            if (file.isDirectory())
                continue;
            String file_id = client.sendFileInfo(file.getName(), Integer.toString(this.peer_id));
            if (file_id.equals("-1"))
                res = false;
            else
                System.out.println(file.getName() + " has id: " + file_id);
        }
        return res;
    }

    private void downloadRequest(String filename, PeerThread requestThread) {
        try {
            requestThread.sendFileRequest(filename);
        } catch (IOException ioe) {
            System.out.println("Something gone wrong with the peer you want to download");
        }
    }

    public void setChatGUI(ChatGUI chatGUI) {
        this.chatGUI = chatGUI;
    }
}
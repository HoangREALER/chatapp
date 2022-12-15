package Peer;

import Client.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
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
    private Set<PeerThread> peerThreads = new HashSet<PeerThread>();
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private PeerServerThread peerServerThread;
    private Client client;

    public Peer (String username, String port, String directoryPath) throws IOException {
        // set host and port
        this.username = username;
        this.peerPort = port;
        this.directoryPath = directoryPath;
        this.peerServerThread = new PeerServerThread(port, this);
        peerServerThread.start();
    }
    public String getFolder() {
        return this.directoryPath;
    }
    public void execute() throws IOException {
        try {
            while (true)
                options();
//                update(this.reader, this.username, this.peerServerThread);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void options() throws Exception {
        System.out.println("> [1]. List all users");
        System.out.println("  [2]. Add yourself to the list");
        System.out.println("  [3]. List all file");
        System.out.println("  [4]. File add to server");
        System.out.println("  [5]. Communicate");
        System.out.println("  [6]. Download file");
        System.out.println("  [7]. Exit");
        String input = this.reader.readLine();
        switch(input) {
            case "1":
                break;
            case "2":
                int peer_id = Integer.valueOf(client.addUser(this.username, this.peerPort));
                if (peer_id == -1) {
                    System.out.println("Add fail");
                    break;
                }
                this.peer_id = peer_id;
                System.out.println("Your id is " + peer_id);
                break;
            case "3":
                break;
            case "4":
                if (this.peer_id == -1) {
                    System.out.println("Must add your self to the league. Use [A]dd options.");
                    break;
                }
                addFiles();
                break;
            case "5":
                update();
                break;
            case "6":
                downloadSetup();
                break;
            case "7":
                removeThreads();
                peerServerThread.removeSubThreads();
                System.exit(0);
                break;
            default:
                break;
        }
    }
    public void update() throws Exception {
        System.out.println("> enter hostname:port# (space separated)(b to back)");
        System.out.println("of peer(s) to send messages to:");
        String input = reader.readLine();
        String[] inputValues = input.split(",\s*");
        if (!input.equals("b")) {
            for (int i = 0; i < inputValues.length; i++) {
                String address[] = inputValues[i].split(":");
                Socket socket = null;
                try {
                    socket = new Socket(address[0], Integer.valueOf(address[1]));
                    PeerThread peerThread = new PeerThread(socket, username, this);
                    peerThreads.add(peerThread);
                } catch (Exception e) {
                    if (socket != null)
                        socket.close();
                    else System.out.println("Invalid input");
                }
            }
        }
        communicate();
    }

    public void removeThreads() {
        Iterator<PeerThread> i = this.peerThreads.iterator();
        while(i.hasNext()){
            PeerThread peerThread = i.next();
            i.remove();
            peerThread.closeEverything();
        }
    }
    public void communicate() {
        try {
            System.out.println("> You can now communicate (c for change):");
            boolean flag = true;
            while (flag) {
                String message = reader.readLine();
                if (message.equals("c")) {
                    flag = false;
                    removeThreads();
                    options();
                } else {
                    peerThreads.forEach(peerThread -> {
                        peerThread.sendMessage(message);
                    });
                }
            }
        } catch (Exception e) {
            //Exception
            e.printStackTrace();
        }
    }

    public String clientPrintUsers() {
        return client.getUserList();
    }
    public int clientAddUser() {
        this.client = new Client(this.host, this.host_port, this);
        this.peer_id = Integer.parseInt(client.addUser(this.username, this.peerPort));
        return this.peer_id;
    }
    public String clientPrintFiles(){
//        JsonReader jsonReader = Json.createReader(new StringReader(list));
//        JsonArray files = jsonReader.readArray();
//        if (files.size() == 0)
//            System.out.println("No files yet");
//        for (int i = 0; i < files.size(); i++) {
//            JsonObject file = files.getJsonObject(i);
//            System.out.println(i + ". - Id: " + file.get("id").toString());
//            System.out.println("   - Filename: " + file.getString("filename"));
//            JsonArray peers = file.getJsonArray("peers");
//            if (peers.size() == 0)
//                System.out.println("   => No peer has the file.");
//            else
//                System.out.println("   => Peers that have the file: ");
//            for (int j = 0; j < peers.size(); j++) {
//                JsonObject peer = peers.getJsonObject(j);
//                System.out.println("      + Peer: " + peer.get("peer_id").toString() +
//                        ", location: " + peer.getString("host") + ":" + peer.getString("port"));
//            }
//
//        }
        return client.getFileList();
    }
    public boolean addFiles () {
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
    private void downloadSetup () throws IOException {
        System.out.println("Select file that you want to download (enter \"filename@host:port\")(b to back): ");
        String input = reader.readLine();
        String[] inputValues = input.split(",\s*");
        if (!input.equals("b")) {
            for (int i = 0; i < inputValues.length; i++) {
                String file_peer[] = inputValues[i].split("@");
                String address[] = file_peer[1].split(":");
                Socket socket = null;
                try {
                    socket = new Socket(address[0], Integer.valueOf(address[1]));
                    PeerThread requestThread = new PeerThread(socket, username, this);
                    downloadRequest(file_peer[0], requestThread);
                    requestThread.closeEverything();
                } catch (Exception e) {
                    if (socket != null)
                        socket.close();
                    else System.out.println("Invalid input");
                }
            }
        }
    }

    private void downloadRequest(String filename, PeerThread requestThread) {
        try {
            requestThread.sendFileRequest(filename);
        } catch (IOException ioe){
            System.out.println("Something gone wrong with the peer you want to download");
        }
    }
}
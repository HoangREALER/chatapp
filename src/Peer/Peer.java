package Peer;

import Client.Client;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
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

    public Peer (String username, String port, String directoryPath) {
        // set host and port
        this.username = username;
        this.peerPort = port;
        this.directoryPath = directoryPath;
        this.client = new Client(this.host, this.host_port, this);
    }
    public String getFolder() {
        return this.directoryPath;
    }
    public void execute() throws IOException {
        System.out.println("> enter username & port # for this peer");
        String[] setupValues = this.reader.readLine().split(" ");

        this.username = setupValues[0];
        this.peerPort = setupValues[1];
        this.peerServerThread = new PeerServerThread(setupValues[1], this);
        peerServerThread.start();
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
                clientPrintUsers(client.getUserList());
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
                clientPrintFiles(client.getFileList());
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

    public void clientPrintUsers(String list) {
        JsonReader jsonReader = Json.createReader(new StringReader(list));
        JsonArray users = jsonReader.readArray();
        if (users.size() == 0)
            System.out.println("No users yet");
        for (int i = 0; i < users.size(); i++) {
            JsonObject user = users.getJsonObject(i);
            System.out.println("User: " + user.getString("username")
                                +" is at " + user.getString("host") + ":" + user.getString("port"));
        }
    }
    public void clientPrintFiles(String list){
        JsonReader jsonReader = Json.createReader(new StringReader(list));
        JsonArray files = jsonReader.readArray();
        if (files.size() == 0)
            System.out.println("No files yet");
        for (int i = 0; i < files.size(); i++) {
            JsonObject file = files.getJsonObject(i);
            System.out.println(i + ". - Id: " + file.get("id").toString());
            System.out.println("   - Filename: " + file.getString("filename"));
            JsonArray peers = file.getJsonArray("peers");
            if (peers.size() == 0)
                System.out.println("   => No peer has the file.");
            else
                System.out.println("   => Peers that have the file: ");
            for (int j = 0; j < peers.size(); j++) {
                JsonObject peer = peers.getJsonObject(j);
                System.out.println("      + Peer: " + peer.get("peer_id").toString() +
                        ", location: " + peer.getString("host") + ":" + peer.getString("port"));
            }

        }
    }
    private void addFiles () {
        File folder = new File(this.directoryPath);
        File[] listOfFiles = folder.listFiles();
        File file;

        for (int i = 0; i < listOfFiles.length; i++) {
            file = listOfFiles[i];
            if (file.isDirectory())
                continue;
            String file_id = client.sendFileInfo(file.getName(), Integer.toString(this.peer_id));
            if (file_id.equals("-1"))
                System.out.println("Fail to add " + file.getName());
            else
                System.out.println(file.getName() + " has id: " + file_id);
        }
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
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Requires 3 parameters for hostname, port and your directory path");
            return;
        }
        String hostname = args[0];
        int host_port = Integer.valueOf(args[1]);
        String directoryPath = args[2];

        Peer mainPeer = new Peer(hostname, host_port, directoryPath);
        mainPeer.execute();
    }
}
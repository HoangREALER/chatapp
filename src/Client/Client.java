package Client;

import Peer.Peer;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    // initialize socket and input output streams
    private String host = null;
    private int host_port;
    private Socket socket;
    private Peer peer;
    private String response = "{}";

    // constructor to put ip address and port
    public Client(String address, int host_port, Peer peer) {
        // set host and port
        this.host = address;
        this.host_port = host_port;
        this.peer = peer;
        try {
            //Connect
            this.socket = new Socket(this.host, this.host_port);
        } catch (UnknownHostException u) {
            System.out.println(u);
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public String getUserList() {
        try {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("request", "list")
                    .add("type", "user")
                    .build();
            ClientThread ct = new ClientThread(this.socket);
            ct.start();
            ct.sendJSON(jsonObject);
            ct.join();
            response = ct.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.response;
    }

    public String addUser(String username, String port) {
        try {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("request", "add")
                    .add("type", "user")
                    .add("username", username)
                    .add("port", port)
                    .build();
            ClientThread ct = new ClientThread(this.socket);
            ct.start();
            ct.sendJSON(jsonObject);
            ct.join();
            response = ct.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.response;
    }

    public String getFileList() {
        try {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("request", "list")
                    .add("type", "file")
                    .build();
            ClientThread ct = new ClientThread(this.socket);
            ct.start();
            ct.sendJSON(jsonObject);
            ct.join();
            response = ct.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.response;
    }

    public String sendFileInfo (String filename, String id) {
        try {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("request", "add")
                    .add("type", "file")
                    .add("filename", filename)
                    .add("peer", id)
                    .build();
            ClientThread ct = new ClientThread(this.socket);
            ct.start();
            ct.sendJSON(jsonObject);
            ct.join();
            response = ct.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.response;
    }

    public  String getUserInfo(String username) {
        try {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("request", "info")
                    .add("type", "user")
                    .add("username", username)
                    .build();
            ClientThread ct = new ClientThread(this.socket);
            ct.start();
            ct.sendJSON(jsonObject);
            ct.join();
            response = ct.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.response;
    }
}




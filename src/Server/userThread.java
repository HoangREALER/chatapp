package Server;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.net.Socket;
import java.util.Set;

public class userThread extends Thread {
    private Socket socket;
    private Server server;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String username = null;
    private boolean flag = true;

    //Hook the thread manager the socket and server
    public userThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    //Begin
    public void run() {
        try {
            //Create buffer to read and write both hook to the client socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            while (flag) {
                JsonObject json = Json.createReader(in).readObject();
                if (json.containsKey("request")) {
                    if (json.getString("request").equals("list") && json.containsKey("type")) {
                        StringWriter stringWriter = new StringWriter();
                        Set<User> users = server.getUsers();
                        switch (json.getString("type")) {
                            case "user":
                                JsonArrayBuilder usersJsonArrayBuilder = Json.createArrayBuilder();
                                users.forEach(user -> {
                                    usersJsonArrayBuilder.add(Json.createObjectBuilder()
                                            .add("id", user.getId())
                                            .add("username", user.getUsername())
                                            .add("host", user.getHost())
                                            .add("port", user.getPort())
                                    );
                                });
                                Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                        .add("list", "success")
                                        .add("user", usersJsonArrayBuilder.build())
                                        .build());
                                response(stringWriter.toString());
                                break;
                            case "file":
                                Set<FileInfo> filesInfo = server.getFiles();
                                JsonArrayBuilder filesJsonArrayBuilder = Json.createArrayBuilder();
                                filesInfo.forEach(file -> {
                                    JsonArrayBuilder peerInfo = Json.createArrayBuilder();
                                    file.getPeerId().forEach(peer_id -> {
                                        String port;
                                        String host;
                                        User user = users.stream()
                                                .filter(u -> u.getId() == peer_id)
                                                .findFirst().orElse(null);
                                        if (user != null) {
                                            peerInfo.add(Json.createObjectBuilder()
                                                    .add("peer_id", peer_id)
                                                    .add("port", user.getPort())
                                                    .add("host", user.getHost())
                                                    .build()
                                            );
                                        }
                                    });
                                    filesJsonArrayBuilder.add(Json.createObjectBuilder()
                                            .add("id", file.getFileId())
                                            .add("filename", file.getFileName())
                                            .add("peers", peerInfo.build())
                                    );
                                });
                                Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                        .add("list", "success")
                                        .add("file", filesJsonArrayBuilder.build())
                                        .build());
                                response(stringWriter.toString());
                            default:
                                break;
                        }
                    } else if (json.getString("request").equals("info") && json.containsKey("type")) {
                        Set<User> users = server.getUsers();
                        Set<FileInfo> filesInfo = server.getFiles();
                        StringWriter stringWriter = new StringWriter();
                        JsonObjectBuilder infoJson = Json.createObjectBuilder();
                        switch (json.getString("type")) {
                            case "user":
                                if (json.containsKey("peer_id")) {
                                    int id = json.getInt("peer_id");
                                    users.forEach(user -> {
                                        if (user.getId() == id)
                                            infoJson.add("username", user.getUsername())
                                                    .add("host", user.getHost())
                                                    .add("port", user.getPort())
                                                    .build();
                                    });
                                }
                                Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                        .add("info", "success")
                                        .add("user", infoJson.build())
                                        .build());
                                response(stringWriter.toString());
                                break;
                            default:
                                break;
                        }
                    } else if (json.getString("request").equals("add")) {
                        if (json.containsKey("type") && json.getString("type").equals("user")) {
                            if (json.containsKey("username") && json.containsKey("port")) {
                                username = json.getString("username");
                                String address = socket.getInetAddress().toString();
                                String port = json.getString("port");
                                int id = server.addUserName(username, address, port);
                                StringWriter stringWriter = new StringWriter();
                                if (id != -1) {
                                    Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                            .add("add", "success")
                                            .add("user", id)
                                            .build());
                                } else {
                                    Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                            .add("add", "fail")
                                            .build());
                                }
                                response(stringWriter.toString());
                            }
                        } else if (json.containsKey("type") && json.getString("type").equals("file")) {
                            if (json.containsKey("filename") && json.containsKey("peer")) {
                                String filename = json.getString("filename");
                                int peer_id = Integer.valueOf(json.getString("peer"));
                                int id = server.addFile(peer_id, filename);
                                StringWriter stringWriter = new StringWriter();
                                if (id != -1) {
                                    Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                            .add("add", "success")
                                            .add("file", id)
                                            .build());
                                } else {
                                    Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                            .add("add", "fail")
                                            .build());
                                }
                                response(stringWriter.toString());
                            }
                        }
                    } else {
                        StringWriter stringWriter = new StringWriter();
                        Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder()
                                .add("add", "not enough parameters")
                                .build());
                        response(stringWriter.toString());
                    }
                }
            }
        } catch (IOException e) {
            closeEverything(socket, in, out);
        } finally {
            closeEverything(socket, in, out);
        }
    }


    //Use buffer to send message
    void response(String message) {
        try {
            out.println(message);
        } catch (Exception e) {
            System.out.println("Message send error");
            closeEverything(socket, in, out);
            e.getStackTrace();
        }
    }

    void closeEverything(Socket socket, BufferedReader in, PrintWriter out) {
        server.removeUser(username, this);
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


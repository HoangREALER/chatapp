package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int port;
    private ServerSocket server = null;
    private Set<User> users = new HashSet<User>();
    private Set<FileInfo> filesInfo = new HashSet<FileInfo>();
    private Set<userThread> userThreads = new HashSet<>();

    //Config class
    public Server(int port) {
        this.port = port;
    }

    //
    public void execute() {
        try {
            server = new ServerSocket(this.port);
            System.out.println("Chat Server is listening on port " + port);
            while (true) {
                Socket socket = server.accept();
                InetAddress ip = socket.getInetAddress();
                int socketLocalPort = socket.getPort();

                //Notify users on arrival
                System.out.println("New user from: " + ip + ":" + socketLocalPort);

                //Create thread managers
                userThread newUser = new userThread(socket, this);

                //Add user to the thread manager
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException e) {
            System.out.println("Error in server");
            e.getStackTrace();
        }
    }


    //driver
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Give Port number plz");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);

        Server server = new Server(port);
        server.execute();
    }

    //Add user on the name list
    public int addUserName(String userName, String host, String port) {
        try {
            Iterator<User> i = users.iterator();
            while(i.hasNext()) {
                if (i.next().getUsername().equals(userName))
                    return -1;
            }
            User user = new User(userName, host, port);
            users.add(user);
            System.out.println("User: " + userName + " joined the league.");
            return user.getId();
        } catch (Exception e) {
            return -1;
        }
    }

    public int addFile(int peer_id, String filename) {
        try {
            Iterator<FileInfo> i = filesInfo.iterator();
            while (i.hasNext()) {
                FileInfo file = i.next();
                if (file.getFileName().equals(filename))
                {
                    if (!file.getPeerId().contains(Integer.valueOf(peer_id)))
                        file.addPeer(peer_id);
                    return file.getFileId();
                }
            }
            FileInfo file = new FileInfo(peer_id, filename);
            filesInfo.add(file);
            System.out.println("Added: " + filename);
            return file.getFileId();
        } catch (Exception e) {
            return -1;
        }
    }

    public Set<User> getUsers() {
        return users;
    }

    public Set<FileInfo> getFiles() {
        return filesInfo;
    }

    //remove user out of the thread and name list
    void removeUser(String userName, userThread aUser) {
        userThreads.remove(aUser);
        users.forEach(user -> {
            if (user.getUsername() == userName)
                users.remove(user);
        });
        System.out.println("The user at " + userName + " has quited");
    }


//    public void closeServerSocket() {
//        try {
//            if (server != null)
//                server.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}

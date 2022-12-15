package ui;

import Peer.Peer;
import Peer.PeerThread;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;

public class MenuGUI extends JFrame {
    private JPanel mainPanel;
    private JList listUsernames;
    private JList listFiles;
    private JButton exitButton;
    private JButton addFilesToButton;
    private JPanel actions;
    private JButton chatButton;
    private JButton downloadButton;
    private JScrollPane userListScroll;
    private JScrollPane fileListScroll;
    private JButton refreshButton;
    private JLabel username;
    private Peer peer;

    public MenuGUI(Peer peer) throws IOException {
        this.peer = peer;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MenuGUI window = new MenuGUI(peer, 0);
                    window.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MenuGUI(Peer peer, int a) {
        this.peer = peer;
        this.username.setText(peer.username);
        initializePanel();
        initializeUsers();
        initializeFiles();
        initializeButtons();
//        String week[]= { "Monday","Tuesday","Wednesday",
//                "Thursday","Friday","Saturday","Sunday"};
//        this.listUsernames.setListData(week);
    }

    private void initializePanel() {
        super.setTitle("Chat like napster menu");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.setSize(new Dimension(1080, 720));
        this.setResizable(true);
    }

    private void initializeUsers() {
        String usersJson = peer.clientPrintUsers();
        JsonReader jsonReader = Json.createReader(new StringReader(usersJson));
        JsonArray users = jsonReader.readArray();
        ArrayList<String> listUsers = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            JsonObject user = users.getJsonObject(i);
            listUsers.add(user.getString("username"));
        }
        this.listUsernames.setListData(listUsers.toArray());
    }
    private void initializeFiles() {
        String filesJson = peer.clientPrintFiles();
        JsonReader jsonReader = Json.createReader(new StringReader(filesJson));
        JsonArray files = jsonReader.readArray();
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < files.size(); i++) {
            JsonObject file = files.getJsonObject(i);
            String s;
            s = file.getString("filename");
            JsonArray peers = file.getJsonArray("peers");
            for (int j = 0; j < peers.size(); j++) {
                JsonObject peer = peers.getJsonObject(j);
                if (j == 0)
                    s = s + "@";
                s = s + peer.getString("host") + ":" + peer.getString("port");
                if (j < peers.size() - 1)
                    s = s + ", ";
            }
            list.add(s);
        }
        this.listFiles.setListData(list.toArray());
    }

    private void initializeButtons() {
        addFilesToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean res = peer.addFiles();
                if (!res)
                    JOptionPane.showMessageDialog(null,
                            "Add fail",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                else JOptionPane.showMessageDialog(null,
                        "All files added");
                initializeFiles();
            }
        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeUsers();
                initializeFiles();
            }
        });
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<String> toChat = listUsernames.getSelectedValuesList();
                if (toChat.isEmpty())
                    JOptionPane.showMessageDialog(null,
                            "Please choose someone to talk to bro",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
//                else {
//
//                }
            }
        });
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<String> toDownload = listFiles.getSelectedValuesList();
                if (toDownload.isEmpty())
                    JOptionPane.showMessageDialog(null,
                            "Bro, u can download the air ? ...",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                else {
                    for (int i = 0; i < toDownload.size(); i++) {
                        String file_info = toDownload.get(i);
                        String file_peer[] = file_info.split("@");
                        String filename = file_peer[0];
                        if (file_peer.length == 1)
                            JOptionPane.showMessageDialog(null,
                                    "No peer to download from",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        else {

                            String address[] = file_peer[1].split("/")[1].split(":");
                            Socket socket = null;
                            try {
                                socket = new Socket(address[0], Integer.valueOf(address[1]));
                                PeerThread requestThread = new PeerThread(socket, username.getText(), peer);
                                downloadRequest(file_peer[0], requestThread);
                                requestThread.closeEverything();
                            } catch (IOException ex) {
                                if (socket != null) {
                                    try {
                                        socket.close();
                                    } catch (IOException exc) {
                                        throw new RuntimeException(exc);
                                    }
                                }
                                JOptionPane.showMessageDialog(null,
                                        "Cannot connect to peer",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MenuGUI.super.dispose();
                System.exit(0);
            }
        });
    }

    private void downloadRequest(String filename, PeerThread requestThread) {
        try {
            requestThread.sendFileRequest(filename);
        } catch (IOException ioe){
            System.out.println("Something gone wrong with the peer you want to download");
        }
    }
}

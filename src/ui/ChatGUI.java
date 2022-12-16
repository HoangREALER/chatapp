package ui;

import Peer.Peer;
import Peer.PeerThread;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JButton sendButton;
    private JTextField msgBox;
    private JTextField currentUsers;
    private JPanel chatPanel;
    private JButton backButton;
    private Peer peer;
    private String[] users;
    private ChatRoom chat;

    public ChatGUI(Peer peer, String[] users) {
        this.peer = peer;
        this.users = users;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame chatFrame = new ChatGUI(peer, users, 0);
                    chatFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ChatGUI(Peer peer, String[] users, int a) {
        this.peer = peer;
        this.peer.setChatGUI(this);
        this.users = users;
        this.chat = new ChatRoom(peer, users);
        initializePanel();
        initializeChat();
        initializeButton();
    }

    private void initializePanel () {
        super.setTitle("Chatbox Boyyyy");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(chatPanel);
        this.pack();
        this.setSize(new Dimension(1080, 720));
        this.setResizable(true);
    }

    private void initializeChat() {
        chatArea.setEditable(false);
        currentUsers.setEditable(false);
        for (int i = 0; i < this.users.length; i++) {
            if (i == 0)
                currentUsers.setText(users[i]);
            else
                currentUsers.setText(currentUsers.getText() + ", " + users[i]);
        }
    }

    private void initializeButton() {
        msgBox.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {

            }

            @Override
            public void keyReleased(KeyEvent arg0) {

            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    String msg = msgBox.getText();
                    if (msg.equals("")) {
                        msgBox.setText("");
                        msgBox.setCaretPosition(0);
                        return;
                    }
                    try {
                        chat.sendMessage(msg);
                        updateChat("["+ peer.username +"]: " + msg);
                        msgBox.setText("");
                        msgBox.setCaretPosition(0);
                    } catch (Exception e) {
                        msgBox.setText("");
                        msgBox.setCaretPosition(0);
                    }
                }
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    chat.peer.removeThreads();
                    ChatGUI.super.dispose();
                    new MenuGUI(peer);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                String msg = msgBox.getText();
                if (msg.equals("")) {
                    msgBox.setText("");
                    msgBox.setCaretPosition(0);
                    return;
                }
                try {
                    chat.sendMessage(msg);
                    updateChat("["+ peer.username +"] : " + msg);
                    msgBox.setText("");
                    msgBox.setCaretPosition(0);
                } catch (Exception ex) {
                    msgBox.setText("");
                    msgBox.setCaretPosition(0);
                }
            }
        });
    }

    public void updateChat(String msg) {
        chatArea.append(msg + "\n");
    }
    class ChatRoom extends Thread{
        class Location {
            public String host;
            public String port;
            public Location(String host, String port) {
                this.host = host;
                this.port = port;
            }
        }
        private Peer peer;
        private Set<Location> location = new HashSet<>();
        public ChatRoom(Peer peer, String[] users) {
            this.peer = peer;
            for (int i = 0; i < users.length; i++)
            {
                String userInfo = peer.clientGetUser(users[i]);
                JsonReader jsonReader = Json.createReader(new StringReader(userInfo));
                JsonObject userInfoJson = jsonReader.readObject();
                location.add(new Location(userInfoJson.getString("host"), userInfoJson.getString("port")));
            }
            location.forEach(eachLocation -> {
                Socket socket = null;
                try {
                    socket = new Socket(eachLocation.host.replace("/",""), Integer.valueOf(eachLocation.port));
                    PeerThread peerThread = new PeerThread(socket, peer.username, peer);
                    peer.peerThreads.add(peerThread);
                } catch (Exception e) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    else System.out.println("Location failed");
                }
            });
        }

        public void sendMessage(String msg) {
            peer.sendMessage(msg);
        }
    }
}

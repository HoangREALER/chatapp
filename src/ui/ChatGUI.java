package ui;

import javax.swing.*;
import java.awt.*;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JButton sendButton;
    private JTextField msg;
    private JTextField currentUsers;
    private JPanel chatPanel;
    private JButton backButton;

    public ChatGUI() {
        super.setTitle("Chat like napster menu");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(chatPanel);
        this.pack();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame menuFrame = new ChatGUI();
                    menuFrame.setVisible(true);
                    menuFrame.setSize(new Dimension(1080, 720));
                    menuFrame.setResizable(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

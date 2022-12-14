package ui;

import javax.swing.*;
import java.awt.*;

public class MenuGUI extends JFrame{
    private JPanel mainPanel;
    private JList listUsernames;
    private JList listFiles;
    private JButton addYourSelfButton;
    private JButton exitButton;
    private JButton addFilesToButton;
    private JPanel actions;
    private JButton chatButton;
    private JButton downloadButton;

    public MenuGUI() {
        super.setTitle("Chat like napster menu");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame menuFrame = new MenuGUI();
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

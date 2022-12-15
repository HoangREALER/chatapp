package ui;

import Peer.Peer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.regex.Pattern;

class RoundedCornerBorder extends AbstractBorder {
    private static final Color ALPHA_ZERO = new Color(0x0, true);

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape border = getBorderShape(x, y, width - 1, height - 1);
        g2.setPaint(ALPHA_ZERO);
        Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
        corner.subtract(new Area(border));
        g2.fill(corner);
        g2.setPaint(Color.GRAY);
        g2.draw(border);
        g2.dispose();
    }

    public Shape getBorderShape(int x, int y, int w, int h) {
        int r = h; //h / 2;
        return new RoundRectangle2D.Double(x, y, w, h, r, r);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(4, 8, 4, 8);
        return insets;
    }
}

public class LoginGUI extends JFrame {
    JFrame jframe;
    JButton loginButton;
    JTextField username;
    JTextField port;
    JLabel usernameError;
    JLabel portError;


    public LoginGUI() throws IOException {
        jframe = new JFrame("Specify username and port");
        username = new JTextField() {
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(getBackground());
                    g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                            0, 0, getWidth() - 1, getHeight() - 1));
                    g2.dispose();
                }
                super.paintComponent(g);
            }

            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setBorder(new RoundedCornerBorder());
            }
        };
        port = new JTextField() {
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(getBackground());
                    g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                            0, 0, getWidth() - 1, getHeight() - 1));
                    g2.dispose();
                }
                super.paintComponent(g);
            }

            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setBorder(new RoundedCornerBorder());
            }
        };

        loginButton = new JButton("LOGIN") {
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(getBackground());
                    g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                            0, 0, getWidth() - 1, getHeight() - 1));
                    g2.dispose();
                }
                super.paintComponent(g);
            }

            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setBorder(new RoundedCornerBorder());
            }
        };
        usernameError = new JLabel();
        portError = new JLabel();

        jframe.setContentPane(new JPanel() {
            BufferedImage bufferedImage = ImageIO.read(this.getClass().getResource("background.jpg"));
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferedImage, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        });
        init();
    }

    public void addEventListeners() {
        //submit button action listener
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (validateport(port.getText()) && validateUsername(username.getText())) {
                        Peer peer = new Peer(username.getText(), port.getText(), "/Users/hoang/IdeaProjects/ChatAppAssignment1/testfolder/peer2");
                        peer.clientAddUser();
                        jframe.dispose();
                        new MenuGUI(peer);
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid input(s)");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input(s)");
                }
            }
        });

        //username validation listener
        username.getDocument().addDocumentListener(new DocumentListener() {

            public void removeUpdate(DocumentEvent e) {
                if (username.getText().length() > 0 && !username.getText().equals("Enter your username")) {
                    if (validateUsername(username.getText())) {
                        usernameError.setForeground(new Color(50, 168, 58));
                        usernameError.setText("Username is valid");
                    } else {
                        usernameError.setForeground(Color.RED);
                        usernameError.setText("Username is not valid");
                    }
                } else {
                    usernameError.setText("");
                }
            }

            public void insertUpdate(DocumentEvent e) {
                if (username.getText().length() > 0 && !username.getText().equals("Enter your username")) {
                    if (validateUsername(username.getText())) {
                        usernameError.setForeground(new Color(50, 168, 58));
                        usernameError.setText("Username is valid");
                    } else {
                        usernameError.setForeground(Color.RED);
                        usernameError.setText("Username is not valid");
                    }
                } else {
                    usernameError.setText("");
                }
            }

            public void changedUpdate(DocumentEvent e) {
                if (username.getText().length() > 0 && !username.getText().equals("Enter your username")) {
                    if (validateUsername(username.getText())) {
                        usernameError.setForeground(new Color(50, 168, 58));
                        usernameError.setText("Username is valid");
                    } else {
                        usernameError.setForeground(Color.RED);
                        usernameError.setText("Username is not valid");
                    }
                } else {
                    usernameError.setText("");
                }
            }
        });

        //port validation listener
        port.getDocument().addDocumentListener(new DocumentListener() {

            public void removeUpdate(DocumentEvent e) {
                if (port.getText().length() > 0 && !port.getText().equals("Enter your port number")) {
                    if (validateport(port.getText())) {
                        portError.setForeground(new Color(50, 168, 58));
                        portError.setText("Port is valid");
                    }
                } else {
                    portError.setText("");
                }
            }

            public void insertUpdate(DocumentEvent e) {
                if (port.getText().length() > 0 && !port.getText().equals("Enter your port number")) {
                    if (validateport(port.getText())) {
                        portError.setForeground(new Color(50, 168, 58));
                        portError.setText("Port is valid");
                    }
                } else {
                    portError.setText("");
                }
            }

            public void changedUpdate(DocumentEvent e) {
                if (port.getText().length() > 0 && !port.getText().equals("Enter your port number")) {
                    if (validateport(port.getText())) {
                        portError.setForeground(new Color(50, 168, 58));
                        portError.setText("Port is valid");
                    }
                } else {
                    portError.setText("");
                }
            }
        });

        username.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                if (username.getText().equals("")) {
                    username.setText("Enter your username");
                    username.setForeground(Color.gray);
                }
            }

            public void focusGained(FocusEvent e) {
                if (username.getText().equals("Enter your username")) {
                    username.setText("");
                    username.setForeground(Color.black);
                }
            }
        });

        port.addFocusListener(new FocusListener() {

            public void focusLost(FocusEvent e) {
                if (port.getText().equals("")) {
                    port.setText("Enter your port number");
                    port.setForeground(Color.gray);
                }
            }

            public void focusGained(FocusEvent e) {
                if (port.getText().equals("Enter your port number")) {
                    port.setText("");
                    port.setForeground(Color.black);
                }
            }
        });
    }

    private boolean validateUsername(String mail) {
        String regExp = "^\\S*$";
        Pattern pattern = Pattern.compile(regExp);
        return pattern.matcher(mail).matches();

    }

    private boolean validateport(String text) {
        portError.setForeground(Color.RED);
        if (!text.matches("^((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$")) {
            portError.setText("Port number is not between 0-65535");
            return false;
        } else
            return true;
    }

    public void init() {
        username.setPreferredSize(new Dimension(250, 35));
        port.setPreferredSize(new Dimension(250, 35));
        loginButton.setPreferredSize(new Dimension(250, 35));
        loginButton.setBackground(new Color(66, 245, 114));
        loginButton.setFocusPainted(false);

        username.setText("Enter your username");
        username.setForeground(Color.gray);
        port.setText("Enter your port number");
        port.setForeground(Color.gray);


        usernameError.setFont(new Font("SansSerif", Font.BOLD, 11));
        usernameError.setForeground(Color.RED);

        portError.setFont(new Font("SansSerif", Font.BOLD, 11));
        portError.setForeground(Color.RED);

        jframe.setLayout(new GridBagLayout());

        Insets textInsets = new Insets(10, 10, 5, 10);
        Insets buttonInsets = new Insets(20, 10, 10, 10);
        Insets errorInsets = new Insets(0, 20, 0, 0);

        GridBagConstraints input = new GridBagConstraints();
        input.anchor = GridBagConstraints.CENTER;
        input.insets = textInsets;
        input.gridy = 1;
        jframe.add(username, input);

        input.gridy = 2;
        input.insets = errorInsets;
        input.anchor = GridBagConstraints.WEST;
        jframe.add(usernameError, input);

        input.gridy = 3;
        input.insets = textInsets;
        input.anchor = GridBagConstraints.CENTER;
        jframe.add(port, input);

        input.gridy = 4;
        input.insets = errorInsets;
        input.anchor = GridBagConstraints.WEST;
        jframe.add(portError, input);

        input.insets = buttonInsets;
        input.anchor = GridBagConstraints.WEST;
        input.gridx = 0;
        input.gridy = 5;
        jframe.add(loginButton, input);

        jframe.setSize(1080, 720);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);
        jframe.setResizable(true);
        jframe.setLocationRelativeTo(null);

        jframe.requestFocus();
        addEventListeners();
    }

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new LoginGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

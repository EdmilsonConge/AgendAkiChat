package agendakichat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
    @author Ayrton e Edmilson
 */
public class Client implements ActionListener {

    JTextField text;
    static JPanel a1;
    static Box vertical = Box.createVerticalBox();
    static JFrame jFrame = new JFrame();
    static DataOutputStream dataOutputStream;
    static JLabel status;
    static Socket socket;
    static Timer connectionChecker;
    static JScrollPane scrollPane;

    Client() {

        jFrame.setLayout(null);

        JPanel p1 = new JPanel();
        p1.setBackground(new Color(45, 45, 45));
        p1.setBounds(0, 0, 450, 70);
        p1.setLayout(null);
        jFrame.add(p1);

        ImageIcon i1 = new ImageIcon("resources/leftarrowIcon.png");
        Image i2 = i1.getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel back = new JLabel(i3);
        back.setBounds(5, 20, 25, 25);
        p1.add(back);

        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ae) {
                if (connectionChecker != null) {
                    connectionChecker.stop();
                }
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        ImageIcon i4 = new ImageIcon("resources/AgendAki-hd.jpg");
        Image i5 = i4.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
        ImageIcon i6 = new ImageIcon(i5);
        JLabel profile = new JLabel(i6);
        profile.setBounds(40, 10, 50, 50);
        p1.add(profile);

        JLabel appName = new JLabel("AgendAki", SwingConstants.CENTER);
        appName.setBounds(200, 15, 250, 40);
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("SAN_SERIF", Font.BOLD, 22));
        p1.add(appName);

        ImageIcon i13 = new ImageIcon("resources/threeDotsIcon.png");
        Image i14 = i13.getImage().getScaledInstance(10, 25, Image.SCALE_DEFAULT);
        ImageIcon i15 = new ImageIcon(i14);
        JLabel morevert = new JLabel(i15);
        morevert.setBounds(420, 20, 10, 25);
        p1.add(morevert);

        JLabel name = new JLabel("Edmilson");
        name.setBounds(110, 15, 100, 18);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SAN_SERIF", Font.BOLD, 18));
        p1.add(name);

        status = new JLabel("Connecting...");
        status.setBounds(110, 35, 150, 18);
        status.setForeground(Color.YELLOW);
        status.setFont(new Font("SAN_SERIF", Font.BOLD, 10));
        p1.add(status);

        a1 = new JPanel();
        a1.setLayout(new BoxLayout(a1, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(a1);
        scrollPane.setBounds(5, 75, 440, 570);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        jFrame.add(scrollPane);

        text = new JTextField();
        text.setBounds(5, 655, 310, 40);
        text.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        jFrame.add(text);

        JButton send = new JButton("Send");
        send.setBounds(320, 655, 123, 40);
        send.setBackground(new Color(37, 211, 102));
        send.setForeground(Color.WHITE);
        send.addActionListener(this);
        send.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        jFrame.add(send);

        jFrame.setSize(450, 700);
        jFrame.setLocation(800, 50);
        jFrame.setUndecorated(true);
        jFrame.getContentPane().setBackground(Color.WHITE);

        jFrame.setVisible(true);

        startConnectionChecker();
    }

    private void startConnectionChecker() {
        connectionChecker = new Timer(5000, e -> checkConnectionStatus());
        connectionChecker.start();
    }

    private void checkConnectionStatus() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            updateStatus("Not Active", Color.RED);
        } else {
            try {
                if (socket.isConnected() && !socket.isClosed() && dataOutputStream != null) {
                    updateStatus("Active Now", Color.GREEN);
                } else {
                    updateStatus("Not Active", Color.RED);
                }
            } catch (Exception ex) {
                updateStatus("Not Active", Color.RED);
            }
        }
    }

    private static void updateStatus(String statusText, Color color) {
        SwingUtilities.invokeLater(() -> {
            status.setText(statusText);
            status.setForeground(color);
        });
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            String out = text.getText();

            if (out.trim().isEmpty()) {
                return;
            }


            if (dataOutputStream == null || socket == null ||
                    socket.isClosed() || !socket.isConnected()) {
                JOptionPane.showMessageDialog(jFrame,
                        "Not connected to server. Cannot send message.",
                        "Connection Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            JPanel p2 = formatLabel(out);

            a1.setLayout(new BorderLayout());

            JPanel right = new JPanel(new BorderLayout());
            right.add(p2, BorderLayout.LINE_END);
            vertical.add(right);
            vertical.add(Box.createVerticalStrut(15));

            a1.add(vertical, BorderLayout.PAGE_START);

            dataOutputStream.writeUTF(out);

            text.setText("");

            jFrame.repaint();
            jFrame.invalidate();
            jFrame.validate();

            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            });

        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("Not Active", Color.RED);
            JOptionPane.showMessageDialog(jFrame,
                    "Failed to send message. Server may be disconnected.",
                    "Send Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static JPanel formatLabel(String out) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JLabel output = new JLabel("<html><p style=\"width: 150px\">" + out + "</p></html>");
        output.setFont(new Font("Tahoma", Font.PLAIN, 16));
        output.setBackground(new Color(37, 211, 102));
        output.setOpaque(true);
        output.setBorder(new EmptyBorder(15, 15, 15, 50));

        jPanel.add(output);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        JLabel time = new JLabel();
        time.setText(simpleDateFormat.format(calendar.getTime()));

        jPanel.add(time);

        return jPanel;
    }

    private static void attemptReconnection() {
        new Thread(() -> {
            int maxAttempts = 5;
            int attempts = 0;

            while (attempts < maxAttempts) {
                try {
                    updateStatus("Reconnecting... (" + (attempts + 1) + "/" + maxAttempts + ")", Color.ORANGE);
                    Thread.sleep(3000);

                    socket = new Socket("127.0.0.1", 6001);
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    updateStatus("Active Now", Color.GREEN);

                    startMessageReceiver(dataInputStream);
                    return;

                } catch (Exception e) {
                    attempts++;
                    if (attempts >= maxAttempts) {
                        updateStatus("Connection Failed", Color.RED);
                    }
                }
            }
        }).start();
    }


    private static void startMessageReceiver(DataInputStream dataInputStream) {
        Thread messageReceiver = new Thread(() -> {
            try {
                while (true) {
                    String msg = dataInputStream.readUTF();
                    JPanel panel = formatLabel(msg);

                    JPanel left = new JPanel(new BorderLayout());
                    left.add(panel, BorderLayout.LINE_START);
                    vertical.add(left);
                    vertical.add(Box.createVerticalStrut(15));
                    a1.add(vertical, BorderLayout.PAGE_START);

                    SwingUtilities.invokeLater(() -> {
                        jFrame.validate();
                        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                        verticalBar.setValue(verticalBar.getMaximum());
                    });
                }
            } catch (IOException e) {
                updateStatus("Not Active", Color.RED);
                System.out.println("Server disconnected: " + e.getMessage());

                attemptReconnection();
            }
        });
        messageReceiver.setDaemon(true);
        messageReceiver.start();
    }

    public static void main(String[] args) {
        new Client();

        try {
            updateStatus("Connecting...", Color.YELLOW);
            socket = new Socket("127.0.0.1", 6001);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            updateStatus("Active Now", Color.GREEN);

            startMessageReceiver(dataInputStream);

        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("Connection Failed", Color.RED);

            int option = JOptionPane.showConfirmDialog(jFrame,
                    "Failed to connect to server. Do you want to retry?",
                    "Connection Error",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                attemptReconnection();
            }
        }
    }
}
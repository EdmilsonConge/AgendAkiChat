package agendakichat;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.io.*;

public class Server implements ActionListener {

    JTextField text;
    JPanel a1;
    static JScrollPane scrollPane;
    static Box vertical = Box.createVerticalBox();
    static JFrame jFrame = new JFrame();
    static DataOutputStream dataOutputStream;
    static JLabel status;
    static Socket clientSocket;
    static Timer connectionChecker;

    Server() {

        jFrame.setLayout(null);

        JPanel p1 = new JPanel();
        p1.setBackground(new Color(45, 45, 45));
        p1.setBounds(0, 0, 450, 70);
        p1.setLayout(null);
        jFrame.add(p1);

        ImageIcon image1 = new ImageIcon("resources/leftarrowIcon.png");
        Image image2 = image1.getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        ImageIcon image3 = new ImageIcon(image2);
        JLabel back = new JLabel(image3);
        back.setBounds(5, 20, 25, 25);
        p1.add(back);

        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ae) {
                if (connectionChecker != null) {
                    connectionChecker.stop();
                }
                System.exit(0);
            }
        });

        JLabel appName = new JLabel("AgendAki", SwingConstants.CENTER);
        appName.setBounds(200, 15, 250, 40);
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("SAN_SERIF", Font.BOLD, 22));
        p1.add(appName);

        ImageIcon i4 = new ImageIcon("resources/ayrtonpfp.jpg");
        Image i5 = i4.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
        ImageIcon i6 = new ImageIcon(i5);
        JLabel profile = new JLabel(i6);
        profile.setBounds(40, 10, 50, 50);
        p1.add(profile);

        ImageIcon i13 = new ImageIcon("resources/threeDotsIcon.png");
        Image i14 = i13.getImage().getScaledInstance(10, 25, Image.SCALE_DEFAULT);
        ImageIcon i15 = new ImageIcon(i14);
        JLabel morevert = new JLabel(i15);
        morevert.setBounds(420, 20, 10, 25);
        p1.add(morevert);

        JLabel name = new JLabel("Ayrton");
        name.setBounds(110, 15, 100, 18);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SAN_SERIF", Font.BOLD, 18));
        p1.add(name);

        // Status label - agora é uma referência estática para poder ser atualizada
        status = new JLabel("Waiting for connection...");
        status.setBounds(110, 35, 150, 18);
        status.setForeground(Color.LIGHT_GRAY);
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
        send.setBackground(new Color(7, 94, 84));
        send.setForeground(Color.WHITE);
        send.addActionListener(this);
        send.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        jFrame.add(send);

        jFrame.setSize(450, 700);
        jFrame.setLocation(200, 50);
        jFrame.setUndecorated(true);
        jFrame.getContentPane().setBackground(Color.WHITE);

        jFrame.setDefaultCloseOperation(3);
        jFrame.setVisible(true);

        startConnectionChecker();
    }

    private void startConnectionChecker() {
        connectionChecker = new Timer(5000, e -> checkConnectionStatus());
        connectionChecker.start();
    }

    // Método para verificar o status da conexão
    private void checkConnectionStatus() {
        if (clientSocket == null || clientSocket.isClosed() || !clientSocket.isConnected()) {
            updateStatus("Waiting for connection...", Color.LIGHT_GRAY);
        } else {
            // Testa se a conexão está realmente ativa
            try {
                // Tenta enviar um ping para verificar se a conexão está ativa
                if (dataOutputStream != null) {
                    // Verifica se o socket ainda está conectado
                    if (clientSocket.isConnected() && !clientSocket.isClosed()) {
                        updateStatus("Active Now", Color.GREEN);
                    } else {
                        updateStatus("Not Active", Color.RED);
                    }
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

            if (dataOutputStream == null || clientSocket == null ||
                    clientSocket.isClosed() || !clientSocket.isConnected()) {
                JOptionPane.showMessageDialog(jFrame,
                        "No client connected. Cannot send message.",
                        "Connection Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            JPanel jPanel2 = formatLabel(out);

            JPanel right = new JPanel(new BorderLayout());
            right.add(jPanel2, BorderLayout.LINE_END);
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
                    "Failed to send message. Client may be disconnected.",
                    "Send Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static JPanel formatLabel(String out) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel output = new JLabel("<html><p style=\"width: 150px\">" + out + "</p></html>");
        output.setFont(new Font("Tahoma", Font.PLAIN, 16));
        output.setBackground(new Color(220, 220, 220));
        output.setOpaque(true);
        output.setBorder(new EmptyBorder(15, 15, 15, 50));

        panel.add(output);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        JLabel time = new JLabel();
        time.setText(sdf.format(calendar.getTime()));

        panel.add(time);

        return panel;
    }

    public static void main(String[] args) {
        new Server();

        try {
            ServerSocket serverSocket = new ServerSocket(6001);
            updateStatus("Waiting for connection...", Color.LIGHT_GRAY);

            while(true) {
                clientSocket = serverSocket.accept(); // Armazena a referência do socket
                updateStatus("Active Now", Color.GREEN);

                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                // Thread para lidar com mensagens recebidas
                Thread messageHandler = new Thread(() -> {
                    try {
                        while(true) {
                            String msg = dataInputStream.readUTF();
                            JPanel panel = formatLabel(msg);

                            JPanel left = new JPanel(new BorderLayout());
                            left.add(panel, BorderLayout.LINE_START);
                            vertical.add(left);
                            vertical.add(Box.createVerticalStrut(15));

                            SwingUtilities.invokeLater(() -> {
                                jFrame.validate();

                                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                                verticalBar.setValue(verticalBar.getMaximum());
                            });
                        }
                    } catch (IOException e) {
                        updateStatus("Not Active", Color.RED);
                        System.out.println("Client disconnected: " + e.getMessage());
                    }
                });

                messageHandler.start();
                messageHandler.join();

                try {
                    if (dataInputStream != null) dataInputStream.close();
                    if (dataOutputStream != null) dataOutputStream.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                clientSocket = null;
                dataOutputStream = null;
                updateStatus("Waiting for connection...", Color.LIGHT_GRAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("Server Error", Color.RED);
        }
    }
}
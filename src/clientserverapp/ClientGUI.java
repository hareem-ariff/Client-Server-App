/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clientserverapp;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;
/**
 *
 * @author  laiba and taha
 */
public class ClientGUI {

    private JFrame frame = new JFrame("💬 Client Chat");
    private JPanel messagePanel = new JPanel();
    private JScrollPane scrollPane;

    private JTextField inputField = new JTextField(35);
    private JButton sendButton = new JButton("Send 💌");
    private JButton disconnectButton = new JButton("Disconnect ❌");
    private JButton filesButton = new JButton("📂 Files");

    private JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private List<String> availableFiles = new ArrayList<>();
    private Client client;

    public ClientGUI(Client client) {
        this.client = client;
        buildGUI();
    }

    private void buildGUI() {
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(100, 100, 100));
        frame.add(statusLabel, BorderLayout.NORTH);

        frame.setSize(600, 500);
        frame.setMinimumSize(new Dimension(500, 400));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(new Color(255, 240, 250));
        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(new LineBorder(new Color(255, 182, 193), 1));

        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(255, 170, 200));
        sendButton.setForeground(Color.white);
        sendButton.setFocusPainted(false);

        disconnectButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        disconnectButton.setBackground(new Color(255, 105, 97));
        disconnectButton.setForeground(Color.white);
        disconnectButton.setFocusPainted(false);
        disconnectButton.setPreferredSize(new Dimension(110, 35));
        disconnectButton.addActionListener(e -> client.disconnect());

        filesButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filesButton.setBackground(new Color(200, 200, 255));
        filesButton.setFocusPainted(false);
        filesButton.addActionListener(e -> client.requestFileList());

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(255, 240, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtons.setBackground(bottomPanel.getBackground());
        leftButtons.add(disconnectButton);
        leftButtons.add(filesButton);

        bottomPanel.add(leftButtons, BorderLayout.WEST);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        sendButton.addActionListener(e -> {
            client.sendMessage(inputField.getText().trim());
            inputField.setText("");
        });
        inputField.addActionListener(e -> {
            client.sendMessage(inputField.getText().trim());
            inputField.setText("");
        });
    }

    public void setConnectionStatus(boolean connected) {
        String title = connected ? "💬 Client Chat - Connected ✅" : "💬 Client Chat - Disconnected ❌";
        String label = connected ? "" : "❌ Server disconnected";
        Color labelColor = connected ? new Color(0, 128, 0) : Color.RED;

        frame.setTitle(title);
        statusLabel.setText(label);
        statusLabel.setForeground(labelColor);

        inputField.setEnabled(connected);
        sendButton.setEnabled(connected);
        filesButton.setEnabled(connected);
    }

    public void addMessage(String text, boolean isUser) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Fira Sans", Font.PLAIN, 14));
        label.setOpaque(false);
        label.setAlignmentX(isUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        messagePanel.add(label);
        messagePanel.add(Box.createVerticalStrut(5));
        messagePanel.revalidate();
        messagePanel.repaint();
        scrollToBottom();
    }

    public void showBubble(String text, boolean isUser) {
        JPanel bubble = new JPanel();
        bubble.setLayout(new BorderLayout());
        bubble.setBackground(isUser ? new Color(255, 220, 233) : new Color(204, 229, 255));
        bubble.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel msg = new JLabel("<html><p style='width: 250px;'>" + text + "</p></html>");
        msg.setFont(new Font("Fira Sans", Font.PLAIN, 14));
        bubble.add(msg, BorderLayout.CENTER);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setBackground(messagePanel.getBackground());
        wrapper.add(bubble);

        messagePanel.add(wrapper);
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.revalidate();
        messagePanel.repaint();
        scrollToBottom();
    }

    public void handleFileList(String fileList) {
        availableFiles.clear();
        if (!fileList.equals("empty")) {
            String[] files = fileList.split(",");
            for (String file : files) {
                availableFiles.add(file.trim());
            }
            showFileSelectionDialog();
        } else {
            addMessage("📂 No files available.", false);
        }
    }

    private void showFileSelectionDialog() {
        if (availableFiles.isEmpty()) {
            return;
        }

        String[] options = availableFiles.toArray(new String[0]);
        String selected = (String) JOptionPane.showInputDialog(
                frame,
                "Select a file to view its content:",
                "Available Files",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected != null) {
            client.sendMessage("file:get:" + selected);
        }
    }

    private void scrollToBottom() {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> vertical.setValue(vertical.getMaximum()));
    }
}

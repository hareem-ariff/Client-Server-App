/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clientserverapp;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 *
 * @author laiba and taha
 */
public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientGUI gui;
    private Thread receiverThread;

    private final String serverAddress = "localhost";
    private final int port = 1234;

    public Client() {
        gui = new ClientGUI(this);
        connectToServer();
    }

    public void connectToServer() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, port), 3000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            gui.setConnectionStatus(true);

            receiverThread = new Thread(this::receiveMessages);
            receiverThread.start();
        } catch (IOException e) {
            gui.addMessage("❌ Could not connect to server.", false);
            gui.setConnectionStatus(false);
        }
    }

    public void sendMessage(String msg) {
        if (!msg.trim().isEmpty() && out != null) {
            String fullMsg = msg.contains(":") ? msg : "msg:" + msg;
            out.println(fullMsg);
            gui.showBubble(fullMsg.substring(4), true);
        }
    }

    public void requestFileList() {
        if (out != null) {
            out.println("file:list");
        }
    }

    private void receiveMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("ack:files:")) {
                    String fileList = line.substring(10);
                    gui.handleFileList(fileList);
                } else if (line.startsWith("ack:msg:")) {
                    gui.showBubble(line.substring(8), false);
                } else if (line.startsWith("msg:")) {
                    gui.showBubble(line.substring(4), false);
                } else {
                    gui.addMessage(line, false);
                }
            }
        } catch (IOException e) {
            // Continue to finally block
        } finally {
            SwingUtilities.invokeLater(() -> {
                gui.addMessage("❌ Server disconnected.", false);
                gui.setConnectionStatus(false);
            });
        }
    }

    public void disconnect() {
        try {
            if (receiverThread != null) {
                receiverThread.interrupt();
            }
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException ignored) {
        } finally {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clientserverapp;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author hareem
 */

public class ServerGUI extends JFrame {

    /* Text area to display log messages */
    private final JTextArea logArea;

    /* Model for displaying connected client list */
    private final DefaultListModel<String> clientListModel;

    /* Constructor to initialize and build the server GUI */
    public ServerGUI() {
        /* Set window title */
        setTitle("📡 Server Dashboard");

        /* Set window size */
        setSize(650, 450);

        /* Center the window on screen */
        setLocationRelativeTo(null);

        /* Exit only this window, not the whole application */
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        /* Use BorderLayout with padding */
        setLayout(new BorderLayout(10, 10));

        /* Add behavior when window is closed */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                /* Shutdown all clients and exit app */
                Server.shutdownAllClients();
                System.exit(0);
            }
        });

        /* Setup the log area */
        logArea = new JTextArea();
        logArea.setEditable(false);  /* Users should not edit logs */
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Server Logs"));  /* Add title to log section */

        /* Setup the connected client list panel */
        clientListModel = new DefaultListModel<>();
        JList<String> clientList = new JList<>(clientListModel);
        JScrollPane clientScroll = new JScrollPane(clientList);
        clientScroll.setPreferredSize(new Dimension(200, 0));  /* Set fixed width */
        clientScroll.setBorder(new TitledBorder("Connected Clients"));  /* Add title */

        /* Add both panels to the main frame */
        add(logScroll, BorderLayout.CENTER);
        add(clientScroll, BorderLayout.EAST);

        /* Display the window */
        setVisible(true);
    }

    /* Append log message to the log area in GUI thread */
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());  /* Auto-scroll */
        });
    }

    /* Add a client ID to the connected clients list */
    public void addClient(String clientId) {
        SwingUtilities.invokeLater(() -> {
            if (!clientListModel.contains(clientId)) {
                clientListModel.addElement(clientId);
            }
        });
    }

    /* Remove a client ID from the connected clients list */
    public void removeClient(String clientId) {
        SwingUtilities.invokeLater(() -> clientListModel.removeElement(clientId));
    }
}


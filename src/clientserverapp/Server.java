/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clientserverapp;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
/**
 *
 * @author hamna
 */
public class Server {
       /* Port number on which the server will listen for client connections */
    private static final int PORT = 1234;

    /* A thread-safe map to keep track of all active clients using their unique ID */
    public static final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    /* Flag to keep the server running; will be set to false during shutdown */
    private static boolean running = true;

    /* Reference to the GUI of the server, used for logging and UI updates */
    public static ServerGUI gui;

    public static void main(String[] args) {
        /* Initialize the server GUI window */
        gui = new ServerGUI();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            /* Log that the server has successfully started */
            log("Server started on port " + PORT);

            /* Add a shutdown hook so that all clients are disconnected cleanly if the server is closed */
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log("Shutdown signal received. Closing server.");
                shutdownAllClients();
            }));

            /* Main loop: continuously accept client connections while the server is running */
            while (running) {
                try {
                    /* Accept a new client socket connection */
                    Socket clientSocket = serverSocket.accept();

                    /* Create a new handler for the connected client */
                    ClientHandler handler = new ClientHandler(clientSocket);

                    /* Start a new thread to handle communication with the client */
                    Thread t = new Thread(handler);
                    t.setName("ClientThread-" + clientSocket.getPort());
                    t.start();

                } catch (SocketException e) {
                    /* Socket closed intentionally during shutdown */
                    break;
                }
            }
        } catch (IOException e) {
            /* If the server fails to start or crashes during runtime */
            log("Server error: " + e.getMessage());
        }
    }

    /* logs messages to console */
    static void log(String message) {
        String timeStamped = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message;
        System.out.println(timeStamped);

        /* Also send the log to the server GUI if it exists */
        if (gui != null) {
            gui.logMessage(timeStamped);
        }
    }

    /* Gracefully shuts down all active client connections and stops the server */
    public static void shutdownAllClients() {
        log("Manually closing all client connections from GUI.");
        running = false;

        /* Call the shutdown method on each connected client */
        for (ClientHandler handler : activeClients.values()) {
            handler.shutdown();
        }
    }
}

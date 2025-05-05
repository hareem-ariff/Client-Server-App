/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Hareem
 */

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
public class Server {
    private static final int PORT = 1234;
    private static final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
    private static boolean running = true;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Server started on port " + PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log("Shutdown signal received. Closing server.");
                running = false;
                for (ClientHandler handler : activeClients.values()) {
                    handler.shutdown();
                }
            }));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                } catch (SocketException e) {
                    break;
                }
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    static void log(String message) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientId;
        private boolean connected = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }

        public void shutdown() {
            connected = false;
            try {
                socket.close();
            } catch (IOException ignored) {}
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                activeClients.put(clientId, this);
                log("Connected: " + clientId);

                String line;
                while (connected && (line = in.readLine()) != null) {
                    log("Received from " + clientId + ": " + line);

                    // Basic format check: key:value
                    if (!line.contains(":")) {
                        out.println("error:Malformed input. Use key:value format.");
                        continue;
                    }
                    out.println("ack:" + line); // echo back with acknowledgement
                }
            } catch (IOException e) {
                log("Connection error with " + clientId);
            } finally {
                shutdown();
                activeClients.remove(clientId);
                log("Disconnected: " + clientId);
            }
        }
    }

}

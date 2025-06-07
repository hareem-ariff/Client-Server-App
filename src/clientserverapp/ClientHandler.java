/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clientserverapp;
import static clientserverapp.Server.log;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import clientserverapp.Server;
/**
 *
 * @author hareem
 */
public class ClientHandler implements Runnable {

    /* Network socket to communicate with the client */
    private Socket socket;

    /* Reader to receive data from client */
    private BufferedReader in;

    /* Writer to send data to client */
    private PrintWriter out;

    /* Unique identifier for the client (IP:port) */
    private String clientId;

    /* Friendly name of the client; defaults to "Guest" */
    private String clientName = "Guest";

    /* Boolean flag to keep connection alive */
    private boolean connected = true;

    /* Writer to save chat logs */
    private BufferedWriter logWriter;

    /* Reference to server GUI for updates */
    public static ServerGUI gui;

    /* Set of invalid or generic names to ignore as proper names */
    private final Set<String> invalidNames = Set.of("ok", "okay", "okie", "no", "nope", "yes", "yeah", "hmm");

    /* Constructor initializes socket and client ID */
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    /* Generates automated reply to user's message */
    private String generateReply(String message) {
        String msg = message.toLowerCase();
        if (msg.contains("hi") || msg.contains("hello")) {
            return "Hello " + clientName + "! 💖 How can I help you today?";
        } else if (msg.contains("time")) {
            return "⏰ Current server time is: " + new SimpleDateFormat("HH:mm:ss").format(new Date());
        } else if (msg.contains("bye")) {
            return "Goodbye " + clientName + "! 👋 Stay awesome!";
        } else if (msg.contains("help")) {
            return "Sure " + clientName + "! 🚧 You can ask about 'time', 'joke', or just say 'hi'.";
        } else if (msg.contains("joke")) {
            return "🤣 Why did the developer go broke? Because he used up all his cache!";
        } else if (msg.matches(".*\\b(ok|okay|okie)\\b.*")) {
            return "👍 Got it, " + clientName + "! Let me know if you need anything else.";
        } else if (msg.matches(".*\\b(no|nope)\\b.*")) {
            return "🙅 Alright, " + clientName + ". Let me know if you change your mind!";
        } else {
            return "🤖 I'm still learning. You said: \"" + message + "\"";
        }
    }

    /* Gracefully closes client resources and socket */
    public void shutdown() {
        connected = false;
        try {
            if (logWriter != null) {
                logWriter.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    /* Saves a message line to the client's log file */
    private void saveToLog(String line) {
        try {
            if (logWriter != null) {
                logWriter.write(line);
                logWriter.newLine();
                logWriter.flush();
            }
        } catch (IOException ignored) {
        }
    }

    /* Handles file-related commands from the client (list/get) */
    private String handleFileCommand(String value) {
        File dir = new File("src/files");
        if (value.equals("list")) {
            if (!dir.exists() || !dir.isDirectory()) {
                return "ack:files:empty";
            }
            String[] files = dir.list();
            return (files == null || files.length == 0) ? "ack:files:empty" : "ack:files:" + String.join(",", files);
        } else if (value.startsWith("get:")) {
            String filename = value.substring(4).trim();
            File target = new File(dir, filename);
            if (!target.exists()) {
                return "ack:msg:❌ File not found: " + filename;
            }
            try {
                String content = new String(Files.readAllBytes(target.toPath()));
                return "ack:msg:📄 " + filename + " content:\n" + content;
            } catch (IOException e) {
                return "ack:msg:❌ Error reading file.";
            }
        } else {
            return "ack:msg:⚠️ Unknown file command.";
        }
    }

    /* Main logic executed in separate thread for each client */
    @Override
    public void run() {
        try {
            /* Initialize input/output streams and log file */
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            logWriter = new BufferedWriter(new FileWriter("chat_log_" + clientId.replace(":", "_") + ".txt"));

            /* Ask client to enter their name */
            out.println("msg:Welcome to the Smart Server! 💬 Please enter your name");
            String nameInput = in.readLine();
            if (nameInput != null) {
                if (nameInput.startsWith("msg:")) {
                    nameInput = nameInput.substring(4).trim();
                }

                String nameLower = nameInput.toLowerCase();
                if (invalidNames.contains(nameLower) || nameLower.matches("hi|hello|yes|no|ok|okay|okie|yeah|nope|hmm")) {
                    clientName = "Guest";
                } else if (!nameInput.isBlank()) {
                    clientName = nameInput;
                }
            }

            /* Confirm connection to client and add to active clients */
            out.println("ack:msg:Hello " + clientName + "! You are now connected to the chat.");
            Server.activeClients.put(clientId, this);
            log("Connected: " + clientName + " (" + clientId + ")");
            Server.gui.addClient(clientName + " (" + clientId + ")");

            /* Start communication loop */
            String line;
            while (connected && (line = in.readLine()) != null) {
                log("[" + clientName + "] " + line);
                saveToLog("Client: " + line);

                /* Ensure message follows key:value format */
                if (!line.contains(":")) {
                    out.println("error:Malformed input. Use key:value format.");
                    continue;
                }

                String[] parts = line.split(":", 2);
                String key = parts[0];
                String value = parts.length > 1 ? parts[1] : "";

                /* Process the client command and generate response */
                String response = switch (key) {
                    case "msg" -> "ack:msg:" + generateReply(value.trim());
                    case "file" -> handleFileCommand(value.trim());
                    default -> "error:Unknown command.";
                };

                out.println(response);
                saveToLog("Server: " + response);
            }
        } catch (IOException e) {
            log("Connection error with " + clientId);
        } finally {
            /* Cleanup and remove client on disconnect */
            shutdown();
            Server.activeClients.remove(clientId);
            Server.gui.removeClient(clientName + " (" + clientId + ")");
            log("Disconnected: " + clientName + " (" + clientId + ")");
        }
    }
}


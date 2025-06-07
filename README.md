# 💬 Java Client-Server Chat Application

This is a multithreaded Java-based client-server chat system with graphical interfaces for both server and client. It enables real-time messaging, file content requests, and displays connected clients with logging support.

---

## ✨ Features

- 📡 Server GUI dashboard showing connected clients and logs
- 💬 Client GUI for sending and receiving messages
- 🤖 Auto-replies to specific keywords
- 📂 File list and content retrieval from server
- 🧵 Multi-client handling using threads
- 🗃️ Client-specific chat log files saved automatically

---

## 🧱 Project Structure

- 📁 `clientserverapp/`
  - 📄 `Client.java` – Handles client socket and communication logic
  - 📄 `ClientGUI.java` – GUI layout and input handling for the client
  - 📄 `Server.java` – Main server entry point and connection manager
  - 📄 `ServerGUI.java` – Server-side GUI displaying logs and clients
  - 📄 `ClientHandler.java` – Threaded handler for each client
- 📁 `files/` – Directory containing `.txt` files clients can access


## 🧑‍💻 Authors

- **Hareem**
- **Laiba**
- **Taha**

---

## 📜 License

This project is for **educational purposes only** and is **not intended for production use**.

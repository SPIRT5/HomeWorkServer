package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );

    public Server(int port, int poolSize) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public void start() {
        System.out.println("Server started on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            // Читаем первую строку запроса
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) return;

            String path = parts[1];

            if (!validPaths.contains(path)) {
                sendNotFound(out);
                return;
            }

            Path filePath = Path.of(".", "public", path);
            String mimeType = Files.probeContentType(filePath);

            // обработка special case: classic.html
            if ("/classic.html".equals(path)) {
                String template = Files.readString(filePath);
                byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
                sendResponse(out, mimeType, content);
                return;
            }

            // обычный случай
            byte[] content = Files.readAllBytes(filePath);
            sendResponse(out, mimeType, content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNotFound(OutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void sendResponse(OutputStream out, String mimeType, byte[] content) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private void shutdown() {
        threadPool.shutdown();
        System.out.println("Server stopped.");
    }
}


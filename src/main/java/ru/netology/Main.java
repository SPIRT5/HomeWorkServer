package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        // GET /messages?last=10
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            String last = request.getQueryParam("last").orElse("not provided");
            String responseBody = "You requested last=" + last;

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseBody;

            responseStream.write(response.getBytes(StandardCharsets.UTF_8));
            responseStream.flush();
        });

        // POST /messages
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            String body = request.getBody();
            String responseBody = "Received body: " + body;

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseBody;

            responseStream.write(response.getBytes(StandardCharsets.UTF_8));
            responseStream.flush();
        });

        server.listen(9999);
    }
}

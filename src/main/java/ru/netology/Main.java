package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static int setPort = Settings.port;
    public final static int POOL_SIZE = 64;

    public static void main(String[] args) {
        final var server = new Server(POOL_SIZE);
        // добавление хендлеров (обработчиков)
        Handler handler = (request, responseStream) -> {
            try {
                final var filePath = Path.of(".", "public", request.getRequestLine().getMethod());
                final var mimeType = Files.probeContentType(filePath);
                final var length = Files.size(filePath);
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, responseStream);
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        server.addHandler("GET", "/", (request, responseStream) -> {
            System.out.println("Hello from GET!");
            responseGood(responseStream);
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            System.out.println("Hello from POST!");
            responseGood(responseStream);
        });

        for (Addresses e : Addresses.values()) {
            server.addHandler(Request.GET, e.getAddress(), handler);
        }
        server.addHandler("GET", "/classic.html", (request, responseStream) -> {
            try {
                final var filePath = Path.of(".", "public", request.getRequestLine().getMethod());
                final var mimeType = Files.probeContentType(filePath);
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.write(content);
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start(setPort);

    }

    public static void responseGood(BufferedOutputStream responseStream) {
        try {
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HandlerConnections implements Runnable {
    private final Socket socket;
    static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    final static int poolSize = 64;

    static final ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);

    HandlerConnections(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");
            final var method = parts[0];
            final var path = parts[1];
            final var body = parts[2];
            Request request = new Request(method, path, body);
            handlersRun(out, request);
            bodyOfRequest(requestLine);
            if (checkSize(parts) || checkValidParts(parts[1], out) || specialCaseForClassic(parts[1], out)) {
                return;
            }
            endOfProcessing(parts[1], out);
        } catch (IOException e) {
            shutdownAndAwaitTermination();
            e.printStackTrace();
        }
    }

    public static void handlersRun(BufferedOutputStream out, Request request) {
        for (String methodName : Server.handlers.keySet()) {
            if (methodName.equals(request.method)) {
                for (String pathName : Server.handlers.get(methodName).keySet()) {
                    if (pathName.equals(request.requestHeader)) {
                        Server.handlers.get(methodName).get(pathName).handle(request, out);
                    }
                }
            }
        }
    }

    public static void bodyOfRequest(String requestLine) {
        final var parts = requestLine.split("\r\n");
        if (parts.length - 2 == 0) {
            System.out.println("Тело запроса = " + parts[parts.length - 1]);
        }
    }

    void shutdownAndAwaitTermination() {
        HandlerConnections.threadPool.shutdown();
        try {
            if (!HandlerConnections.threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                HandlerConnections.threadPool.shutdownNow();
                if (!HandlerConnections.threadPool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            HandlerConnections.threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static boolean checkSize(String[] parts) {
        return parts.length != 3;
    }

    public static boolean checkValidParts(String path, BufferedOutputStream out) throws IOException {
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return true;
        }
        return false;
    }

    public static boolean specialCaseForClassic(String path, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", path);//к названию файла добавляется папка паблик для считывания пути и точка отделяет тип файла от названия
        // special case for classic
        if (path.equals("/classic.html")) {      //если вторая часть соотв-ет /classic.html
            final var template = Files.readString(filePath); //считываем путь файла
            final var content = template.replace(     //заменяем содержание {time} реальным указанием времени, переводим всё в массив байт
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((                               //выводим на экран 200 и информацию о типе файла, размере содержания и закрываем всё, уходя на новую итерацию
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + Files.probeContentType(filePath) + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);  //выводим само содержание файла /classic.html
            out.flush();
            return true;
        }
        return false;
    }

    public static void endOfProcessing(String path, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", path);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + Files.probeContentType(filePath) + "\r\n" +
                        "Content-Length: " + Files.size(filePath) + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}




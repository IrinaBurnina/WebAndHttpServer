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
    private Socket socket;
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
            if (checkSize(parts) || checkValidParts(parts[1], out) || specialCaseForClassic(parts[1], out)) {
                return;
            }
            endOfProcessing(parts[1], out);
        } catch (IOException e) {
            shutdownAndAwaitTermination();
            e.printStackTrace();
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
        if (!validPaths.contains(path)) { //если список допустимых файлов не содержит тот,
            // что только что поступил в запросе, то буфер вернет 404
            out.write((                   //затем закроет ресурсы и вернет в начало цикла
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
        out.write(( //выводим 200 + тип файла +длину пути= всё в виде массива байт
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + Files.probeContentType(filePath) + "\r\n" +//записываем тип файла, чтобы правильно его открыть с помощью соответствующего приложения
                        "Content-Length: " + Files.size(filePath) + "\r\n" +  // записываем размер полного пути к файлу
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out); //копируем файл, находящийся по адресу файлпафь  в буфер (выходную папку) и закрываем
        out.flush();
    }
}




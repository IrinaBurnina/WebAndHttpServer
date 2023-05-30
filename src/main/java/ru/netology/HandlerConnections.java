package ru.netology;

import org.apache.http.NameValuePair;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HandlerConnections implements Runnable {
    private final Socket socket;
    public static final String GET = "GET";
    public static final String POST = "POST";
    static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    final static int poolSize = 64;

    static final ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
    static Map<String, List<NameValuePair>> nameValuePairMap = new ConcurrentHashMap<>();

    HandlerConnections(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        final var allowedMethods = List.of(GET, POST);
        try (//final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            //RequestLine requestLine=parseRequestLine(in);


// лимит на request line + заголовки
            final var limit = 4096;

            in.mark(limit);
            //bufferedInputStream.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);
            // System.out.println(in.read());

// ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }
// читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }

            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                return;
            }

            final var pathOfRequestLine = requestLine[1];
            if (!pathOfRequestLine.startsWith("/")) {
                badRequest(out);
                return;
            }
            System.out.println("путь! реквест лайн 2 кусок" + pathOfRequestLine);


// ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

// отматываем на начало буфера
            in.reset();
// пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println("после перемотки " + headers);//[]-лист данных с парами "Ключ: значение,"   -ЗАГоЛОВКИ

            // для GET тела нет
            String body = null;
            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    body = new String(bodyBytes);


                    System.out.println("ПОЛЕЗНЫЕ ДАННЫЕ запроса параметры запроса QUERY пары имя=значение, разделенные /n/r " + body);


                    String[] pairs = body.split("\r\n");

                    for (String line : pairs) {
                        String[] nameAndValue = line.split("=");
                        List<NameValuePair> list = new ArrayList<>();
                        NameValuePair pair = new NameValuePair() {
                            @Override
                            public String getName() {
                                return nameAndValue[0];
                            }

                            @Override
                            public String getValue() {
                                return nameAndValue[1];
                            }
                        };
                        if (nameValuePairMap.containsKey(nameAndValue[0])) {
                            nameValuePairMap.get(nameAndValue[0]).add(pair);
                        } else {
                            list.add(pair);
                            nameValuePairMap.put(nameAndValue[0], list);
                        }
                    }
                }
            }

            RequestLine lineOfRequest = new RequestLine(method, pathOfRequestLine, requestLine[2]);
            Request request = new Request(lineOfRequest, headers, body);
            //System.out.println(request.getQueryParam("name").toString());
            System.out.println(request.getQueryParams().toString());
            handlersRun(out, lineOfRequest);

            // bodyOfRequest(requestLine);
//            if (checkSize(parts) || checkValidParts(parts[1], out) || specialCaseForClassic(parts[1], out)) {
//                return;
//            }
//            endOfProcessing(parts[1], out);
        } catch (IOException e) {
            shutdownAndAwaitTermination();
            e.printStackTrace();
        }
    }

    public static void handlersRun(BufferedOutputStream out, RequestLine requestLine) {
        for (String methodName : Server.handlers.keySet()) {
            if (methodName.equals(requestLine.method)) {
                for (String pathName : Server.handlers.get(methodName).keySet()) {
                    if (pathName.contains(requestLine.pathToResource)) {
                        Server.handlers.get(methodName).get(pathName).handle(requestLine, out);
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

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public static RequestLine parseRequestLine(BufferedReader in) throws IOException {
        final var requestLineIn = in.readLine();
        final var parts = requestLineIn.split(" ");
        final var methodInRLine = parts[0];
        final var pathInRLine = parts[1];
        final var versionOfProtocol = parts[2];
        return new RequestLine(methodInRLine, pathInRLine, versionOfProtocol);
    }
}




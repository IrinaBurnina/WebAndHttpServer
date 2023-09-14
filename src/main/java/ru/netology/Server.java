package ru.netology;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService executorService;
    private final Map<String, Map<String, Handler>> handlers;

    public Server(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        handlers = new ConcurrentHashMap<>();
    }

    public void start(int port) {
        System.out.println("Server is starting...");
        try (final var serverSocket = new ServerSocket(port)) {
            do {
                final var socket = serverSocket.accept();
                executorService.submit(() -> {
                    try {
                        connection(socket);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } while (true);
        } catch (IOException e) {
            System.out.println("Socket closed exception");
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> map = new ConcurrentHashMap<>();
        map.put(path, handler);
        handlers.put(method, map);
        System.out.println("Метод обработки " + method + " добавлен.");
    }

    public void connection(Socket socket) throws Exception {
        System.out.println(Thread.currentThread().getName());
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Request.createRequest(in);
            if (request == null) {
                badRequest(out);
            } else if (!handlers.containsKey(request.getRequestLine().getMethod())) {
                resourceNotFound(out);
            } else {
                handlersRun(out, request);
                System.out.println(request.getFullPath()[0] + "?" + request.getFullPath()[1] + "- getfullpath");
                System.out.println(request.getQueryParams() + "  - getQueryParams");
                System.out.println(request.getQueryParam("value") + "   - getQueryParam");
                System.out.println(request.getPostParam("value") + "  -getPostParam");
                System.out.println(request.getPostParams() + " - getPostParams");
                System.out.println(request.getPart("value") + "  -getPart");
                System.out.println(request.getParts() + "  - getParts");

            }
        } catch (Exception e) {
//        (IOException| FileUploadException e) {
            System.out.println(e.getMessage());
            System.out.println("Handler exception");
            e.printStackTrace();
        }

    }

    public void handlersRun(BufferedOutputStream out, Request request) throws IOException {
        Map<String, Handler> map = handlers.get(request.getRequestLine().getMethod());
        String requestPath = request.getFullPath()[0];
        for (String path : map.keySet()) {
            if (requestPath.contains(path)) {
                Handler handler = map.get(path);
                handler.handle(request, out);
            }
        }
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

    private static void resourceNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Resource Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}

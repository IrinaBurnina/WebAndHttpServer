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
    private static Map<String, Map<String, Handler>> handlers;

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
        if (handlers.containsKey(method)) {
            if (handlers.get(method).containsKey(path)) {
                handlers.get(method)
                        .replace(path, handler);
            }
            handlers.get(method).put(path, handler);
        } else {
            map.put(path, handler);
            handlers.put(method, map);
        }
        System.out.println("Метод обработки " + method + "  " + path + " добавлен.");
    }

    public void connection(Socket socket) {
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Request.createRequest(in);
            if (request == null) {
                badRequest(out);
            } else if (!handlers.containsKey(request.getRequestLine().getMethod())) {
                resourceNotFound(out);
            }
            handlersRun(out, request);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public void handlersRun(BufferedOutputStream out, Request request) throws IOException {
        Map<String, Handler> map = handlers.get(request.getRequestLine().getMethod());
        String requestPath = request.getRequestLine().getPathToResource().split("\\?")[0];
        for (String path : map.keySet()) {
            if (requestPath.contains(path)) {
                Handler handler = map.get(path);
                RequestParser requestParser = request.parser(request, (BufferedInputStream) request.getInputStream());
                requestParser.getPath();
                System.out.println(requestParser.getQueryParam("value"));
                requestParser.getQueryParams();
                System.out.println(requestParser.getPostParam("value"));
                requestParser.getPostParams();
                System.out.println(requestParser.getPart("value"));
                requestParser.getParts();
                System.out.println(requestParser);
                handler.handle(request, out);
            }
        }
        if (!map.containsKey(requestPath)) {
            System.out.println("Обработка такого пути не зарегистрирована.");
        }
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void resourceNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Resource Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}

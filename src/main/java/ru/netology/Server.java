package ru.netology;

import org.apache.commons.fileupload.FileUploadException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
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

    public void startServer(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> {
                    try {
                        connection(socket);
                    } catch (IOException e) {
                        System.out.println("Socket closed exception, disconnect");
                        e.printStackTrace();
                    }
                });
            }
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

    public void connection(Socket socket) throws IOException {
        System.out.println(Thread.currentThread().getName());
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Request.createRequest(in);
            if (request == null) {
                badRequest(out);
            } else if (!handlers.containsKey(request.requestLine.getMethod())) {
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
        } catch (IOException | FileUploadException e) {
            System.out.println(e.getMessage());
            System.out.println("Handler exception");
            e.printStackTrace();
        }

    }

    public void handlersRun(BufferedOutputStream out, Request request) throws IOException {
        Map<String, Handler> map = handlers.get(request.requestLine.getMethod());
        String requestPath = request.getFullPath()[0];
        for (String path : map.keySet()) {
            if (requestPath.contains(path)) {
                Handler handler = map.get(path);
                handler.handle(request, out);
            }
            goodRequest(out);
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

    private static void goodRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }
}

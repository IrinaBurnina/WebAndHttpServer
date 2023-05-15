package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server() {
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                final var socket = serverSocket.accept();
                HandlerConnections handler = new HandlerConnections(socket);
                HandlerConnections.threadPool.execute(handler);
            }
        } catch (IOException e) {
            HandlerConnections.threadPool.shutdown();
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> map = new ConcurrentHashMap<>();
        map.put(path, handler);
        handlers.put(method, map);
        System.out.println("Метод обработки " + method + " добавлен.");
    }
}

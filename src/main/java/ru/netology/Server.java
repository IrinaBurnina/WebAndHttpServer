package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
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
}

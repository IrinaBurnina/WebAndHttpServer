package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                final var socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                Handler.threadPool.execute(handler);
            }
        } catch (IOException e) {
            Handler.threadPool.shutdown();
            e.printStackTrace();
        }
    }
}

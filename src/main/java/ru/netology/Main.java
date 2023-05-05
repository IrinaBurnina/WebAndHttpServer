package ru.netology;

public class Main {
    public static int setPort = 9999;

    public static void main(String[] args) {
        Server server = new Server(setPort);
        server.run();
    }
}



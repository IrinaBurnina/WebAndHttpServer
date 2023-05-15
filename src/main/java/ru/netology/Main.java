package ru.netology;

public class Main {
    public static int setPort = 9999;

    public static void main(String[] args) {
        final var server = new Server();
        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            System.out.println("Hello from GET!");// TODO: handlers code
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            System.out.println("Hello from POST!");// TODO: handlers code
        });
        server.listen(setPort);
    }
}




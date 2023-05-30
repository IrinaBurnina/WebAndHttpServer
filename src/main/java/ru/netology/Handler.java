package ru.netology;

import java.io.BufferedOutputStream;

public interface Handler {
    void handle(RequestLine requestLine, BufferedOutputStream bufferedOutputStream);
}

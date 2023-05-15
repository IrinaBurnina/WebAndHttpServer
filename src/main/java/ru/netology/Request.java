package ru.netology;

public class Request {
    public String method;
    public String requestHeader;
    public String entityHeader;

    public Request(String method, String requestHeader, String entityHeader) {
        this.method = method;
        this.requestHeader = requestHeader;
        this.entityHeader = entityHeader;
    }
}

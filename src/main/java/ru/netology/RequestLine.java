package ru.netology;

public class RequestLine {
    public String method;
    public String pathToResource;
    public String versionOfProtocol;

    public RequestLine(String method, String pathToResource, String versionOfProtocol) {
        this.method = method;
        this.pathToResource = pathToResource;
        this.versionOfProtocol = versionOfProtocol;
    }

}

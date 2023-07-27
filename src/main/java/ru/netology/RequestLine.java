package ru.netology;

public class RequestLine {
    private String method;
    private String pathToResource;
    private String versionOfProtocol;

    public RequestLine(String method, String pathToResource, String versionOfProtocol) {
        this.method = method;
        this.pathToResource = pathToResource;
        this.versionOfProtocol = versionOfProtocol;
    }

    public String getMethod() {
        return method;
    }

    public String getPathToResource() {
        return pathToResource;
    }

    public String getVersionOfProtocol() {
        return versionOfProtocol;
    }

    @Override
    public String toString() {
        return "RequestLine{" +
                "method='" + method + '\'' +
                ", pathToResource='" + pathToResource + '\'' +
                ", versionOfProtocol='" + versionOfProtocol + '\'' +
                '}';
    }
}

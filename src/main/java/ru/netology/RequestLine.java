package ru.netology;

import org.apache.http.ProtocolVersion;

public class RequestLine implements org.apache.http.RequestLine {
    private String method;
    private String pathToResource;
    private String versionOfProtocol;
    private ProtocolVersion protocolVersion;

    public RequestLine(String method, String pathToResource, String versionOfProtocol) {
        this.method = method;
        this.pathToResource = pathToResource;
        this.versionOfProtocol = versionOfProtocol;
    }

    public RequestLine(String method, String pathToResource, ProtocolVersion protocolVersion) {
        this.method = method;
        this.pathToResource = pathToResource;
        this.protocolVersion = protocolVersion;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String getUri() {
        return pathToResource;
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

package ru.netology;

import org.apache.commons.fileupload.RequestContext;
import org.apache.http.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RequestContextImpl implements RequestContext {
    //    private final Request request;
    private final HttpRequest httpRequest;
    private final String contentType;
    private final int contentLength;
    private final InputStream inputStream;

    public RequestContextImpl(HttpRequest httpRequest, String contentType, int contentLength, InputStream inputStream) {
        this.httpRequest = httpRequest;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.inputStream = inputStream;
    }


    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }
}

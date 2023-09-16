package ru.netology;

import org.apache.commons.fileupload.UploadContext;

import java.io.InputStream;
import java.nio.charset.Charset;

public class RequestContextImpl implements UploadContext {
    private final Request request;

    public RequestContextImpl(Request request) {
        this.request = request;
    }

    @Override
    public long contentLength() {
        return request.contentLength();
    }

    @Override
    public String getCharacterEncoding() {
        return Charset.defaultCharset().name();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public InputStream getInputStream() {
        return request.getInputStream();
    }

}

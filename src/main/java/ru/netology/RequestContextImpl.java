package ru.netology;

import org.apache.commons.fileupload.UploadContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class RequestContextImpl implements UploadContext {
    private final long contentLength;
    private final Charset characterEncoding;
    private final String contentType;
    private final BufferedInputStream inputStream;

    public RequestContextImpl(long contentLength, Charset characterEncoding, String contentType, BufferedInputStream inputStream) {
        this.characterEncoding = characterEncoding;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.inputStream = inputStream;
    }

    @Override
    public long contentLength() {
        return this.getContentLength();
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding.name();
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public int getContentLength() {
        return (int) this.contentLength;// TODO если превышение по размерам , тогда использовать лонг
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.inputStream;
    }
    //    private final Request request;

}

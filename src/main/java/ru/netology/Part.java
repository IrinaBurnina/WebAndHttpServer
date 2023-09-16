package ru.netology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class Part implements javax.servlet.http.Part {
    private final boolean isField;
    private String value;
    private byte[] data;

    public Part(boolean isField, byte[] data) {
        this.isField = isField;
        this.data = data;
    }

    public Part(boolean isField, String value) {
        this.isField = isField;
        this.value = value;
    }

    public String getValue() {
        if (!isField) {
            return null;
        }
        return value;
    }

    public byte[] getData() {
        if (isField) {
            return null;
        }
        return data;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getSubmittedFileName() {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public void write(String fileName) throws IOException {

    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }
}

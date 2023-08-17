package ru.netology;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

import java.io.*;

public class PartImplFileItem implements FileItem {
    byte[] content;
    boolean isFormField;

    public PartImplFileItem(byte[] content, boolean isFormField) {
        this.content = content;
        this.isFormField = isFormField;
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
    public boolean isInMemory() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public byte[] get() {
        return new byte[0];
    }

    @Override
    public String getString(String s) throws UnsupportedEncodingException {
        return null;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public void write(File file) throws Exception {

    }

    @Override
    public void delete() {

    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public void setFieldName(String s) {

    }

    @Override
    public boolean isFormField() {
        return false;
    }

    @Override
    public void setFormField(boolean b) {

    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public FileItemHeaders getHeaders() {
        return null;
    }

    @Override
    public void setHeaders(FileItemHeaders fileItemHeaders) {

    }
}

package ru.netology;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

import java.io.*;

public class PartImplFileItem implements FileItem {
    String fieldName;
    String contentType;
    boolean isFormField;
    String fileName;

    public PartImplFileItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public String getName() {
        return this.fileName;
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
        return this.fieldName;
    }

    @Override
    public void setFieldName(String s) {

    }

    @Override
    public boolean isFormField() {
        return this.isFormField;
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

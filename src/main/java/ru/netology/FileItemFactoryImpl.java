package ru.netology;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeaders;

import java.io.*;

public class FileItemFactoryImpl implements FileItemFactory {

    public FileItemFactoryImpl() {

    }

    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new FileItem() {
            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public boolean isInMemory() {
                return true;
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
            public String getString(String encoding) throws UnsupportedEncodingException {
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
                return fieldName;
            }

            @Override
            public void setFieldName(String name) {

            }

            @Override
            public boolean isFormField() {
                return isFormField;
            }

            @Override
            public void setFormField(boolean state) {

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
            public void setHeaders(FileItemHeaders headers) {

            }
        };
    }
}

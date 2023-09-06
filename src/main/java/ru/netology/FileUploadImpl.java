package ru.netology;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;

public class FileUploadImpl extends FileUploadBase {// по наследству есть методы класса FileUploadBase
    String headerEncoding;
    String boundary;

    public FileUploadImpl() {

    }

    @Override
    public FileItemFactory getFileItemFactory() {
        return new FileItemFactory() {
            @Override
            public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
                return new PartImplFileItem(fieldName, contentType, isFormField, fileName);
            }
        };
    }

    @Override
    public void setFileItemFactory(FileItemFactory fileItemFactory) {

    }
}

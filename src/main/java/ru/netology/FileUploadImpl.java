package ru.netology;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;

public class FileUploadImpl extends FileUploadBase {// по наследству есть методы класса FileUploadBase

    public FileUploadImpl() {

    }

    @Override
    public FileItemFactory getFileItemFactory() {
        return null;
    }

    @Override
    public void setFileItemFactory(FileItemFactory fileItemFactory) {

    }
}

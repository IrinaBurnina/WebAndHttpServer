package ru.netology;

import org.apache.commons.fileupload.*;

import java.io.IOException;

public class FileUploadImpl extends FileUpload {// по наследству есть методы класса FileUploadBase

//    private static final String POST_METHOD = "POST";

//    public static boolean isMultipartContent(Request request) {
//        return "POST".equalsIgnoreCase(request.getRequestLine().getMethod()) &&
//                FileUploadBase.isMultipartContent(new RequestContextImpl(request));
//    }

    public FileUploadImpl() {
    }

//    public FileUploadImpl(FileItemFactory fileItemFactory) {
//        super(fileItemFactory);
//    }

//    public List<FileItem> parseRequest(Request request) throws FileUploadException, FileUploadException {
//        return this.parseRequest(new RequestContextImpl(request));
//    }
//
//    public Map<String, List<FileItem>> parseParameterMap(Request request) throws FileUploadException {
//        return this.parseParameterMap(new RequestContextImpl(request));
//    }

    public FileItemIterator getItemIterator(Request request) throws FileUploadException, IOException {
        return super.getItemIterator(new RequestContextImpl(request));
    }

    @Override
    public FileItemFactory getFileItemFactory() {
        return null;
    }

    @Override
    public void setFileItemFactory(FileItemFactory fileItemFactory) {

    }
}

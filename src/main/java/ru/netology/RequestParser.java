package ru.netology;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.util.Streams;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestParser implements UploadContext {
    private List<NameValuePair> queryParams;
    private List<NameValuePair> bodyParams;
    private Map<String, List<Part>> multiParts;
    private String[] path = new String[2];
    private List<String> headers;
    private String contentType;
    private String contentLength;
    private BufferedInputStream in;

    public RequestParser(List<NameValuePair> queryParams, List<NameValuePair> bodyParams, Map<String, List<Part>> multiParts, String[] path, List<String> headers, String contentType, String contentLength, BufferedInputStream in) {
        this.queryParams = queryParams;
        this.bodyParams = bodyParams;
        this.multiParts = multiParts;
        this.path = path;
        this.headers = headers;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.in = in;
    }

    private List<NameValuePair> parseQuery(RequestLine requestLine) {
        path = requestLine.getPathToResource().split("\\?");
        if (path.length > 1) {
            return URLEncodedUtils.parse(path[1], StandardCharsets.UTF_8);
        }
        return new ArrayList<>();
    }


    public static Map<String, List<Part>> parseBodyWithFiles(Request requestContext) {
        FileUploadImpl fileUpload = new FileUploadImpl();
        System.out.println("fileuload created");//сработало
        Map<String, List<Part>> parts = new HashMap<>();
        System.out.println("map parts created");//сработало
        try {
            FileItemIterator iterStream = fileUpload.getItemIterator(requestContext);
            System.out.println("iterstream created");
            while (iterStream.hasNext()) {
                System.out.println("cycle hasnext is started");
                FileItemStream item = iterStream.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                //PartImplFileItem part;
                Part part;
                if (!item.isFormField()) {
                    byte[] content = stream.readAllBytes();
                    part = new Part(item.isFormField(), content);
                    // part = new PartImplFileItem(name,item.getContentType(),item.isFormField(),item.getName());
                } else {
                    String value = Streams.asString(stream);
                    // part = new PartImplFileItem(name,item.getContentType(),item.isFormField(),item.getName());
                    part = new Part(item.isFormField(), value);
                }
                List<Part> listParts = parts
                        .computeIfAbsent(name, k -> new ArrayList<>());
                listParts.add(part);
            }
        } catch (FileUploadException | IOException e) {
            System.out.println("fileupload exception");
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
//            }
        return parts;
    }


//    private static Optional<String> extractHeader(List<String> headers, String header) {
//        return headers.stream()
//                .filter(o -> o.startsWith(header))
//                .map(o -> o.substring(o.indexOf(" ")))
//                .map(String::trim)
//                .findFirst();
//    }
//
//    private static int indexOf(byte[] array, byte[] target, int start, int max) {
//        outer:
//        for (int i = start; i < max - target.length + 1; i++) {
//            for (int j = 0; j < target.length; j++) {
//                if (array[i + j] != target[j]) {
//                    continue outer;
//                }
//            }
//            return i;
//        }
//        return -1;
//    }

    String[] getPath() {
        return path;
    }

    private List<NameValuePair> getValueParamByName(String name, List<NameValuePair> queryParams) {
        List<NameValuePair> list = new ArrayList<>();
        for (NameValuePair nameValue : queryParams) {
            if (name.equals(nameValue.getName())) {
                list.add(nameValue);
            }
        }
        return list;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getPostParams() {
        return bodyParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return getValueParamByName(name, queryParams);
    }

    public List<NameValuePair> getPostParam(String name) {
        return getValueParamByName(name, bodyParams);
    }

    public List<Part> getPart(String name) {
        return Collections.unmodifiableList((multiParts.getOrDefault("name", new ArrayList<>())));
    }

    public Map<String, List<Part>> getParts() {
        return Collections.unmodifiableMap(multiParts);
    }

    // public String getHeader(String headerName) {
    // var optionalHeader = extractHeader(headers, headerName);
    //   return optionalHeader.orElse(null);
    //}
    @Override
    public long contentLength() {
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        return Charset.defaultCharset().toString();
    }

    @Override
    public String getContentType() {
        // return getHeader("Content-Type");
        return contentType;
    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(contentLength);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in;
    }
}

package ru.netology;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.util.Streams;
import org.apache.http.*;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request implements UploadContext {
    protected static RequestLine requestLine;
    private final List<String> headers;
    private final List<NameValuePair> body;
    private final List<NameValuePair> query;
    //    private static BufferedInputStream in;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final List<String> allowedMethods = List.of(GET, POST);
    private static List<NameValuePair> queryParams = new ArrayList<>();
    private static List<NameValuePair> bodyParams = new ArrayList<>();
    private static Map<String, List<Part>> multiParts = new HashMap<>();
    private static String[] fullPath = new String[2];

    public Request(RequestLine requestLine, List<String> headers, List<NameValuePair> queryParams,
                   List<NameValuePair> bodyParams, Map<String, List<Part>> multiParts) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.query = queryParams;
        this.body = bodyParams;
        this.multiParts = multiParts;
    }

    public Request(RequestLine requestLine, List<String> headers, List<NameValuePair> queryParams) {
        this(requestLine, headers, queryParams, null, null);
    }

    public Request(RequestLine requestLine, List<String> headers, List<NameValuePair> queryParams, List<NameValuePair> bodyParams) {
        this(requestLine, headers, queryParams, bodyParams, null);
    }

    public static Request createRequest(BufferedInputStream in) throws IOException {
        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

// ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }
// читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }
        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }
        final var pathOfRequestLine = requestLine[1];
        if (!pathOfRequestLine.startsWith("/")) {
            return null;
        }
// ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }
// отматываем на начало буфера
        in.reset();
// пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
// для GET тела нет
        String bodyWithParams;
        RequestLine lineOfRequest = new RequestLine(requestLine[0], requestLine[1], requestLine[2]);
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
// вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            final var contentType = extractHeader(headers, "Content-Type");
            if (contentLength.isPresent()) { //если тело есть
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                bodyWithParams = new String(bodyBytes);
                queryParams = parseQuery(lineOfRequest);
                if (contentType != null) {
                    if (contentType.get().startsWith("application/x-www-form-urlencoded")) {
                        bodyParams = URLEncodedUtils.parse(bodyWithParams, StandardCharsets.UTF_8);
                        return new Request(lineOfRequest, headers, queryParams, bodyParams);
                    } else if (contentType.get().startsWith("multipart/form-data")) {
                        RequestContext requestContext = new RequestContextImpl(length, StandardCharsets.UTF_8, contentType.get(), in);
                        multiParts = parseBodyWithFiles(requestContext);
                        return new Request(lineOfRequest, headers, queryParams, null, multiParts);
                    }
                    return new Request(lineOfRequest, headers, queryParams);
                }
            }
        }
        return new Request(lineOfRequest, headers, parseQuery(lineOfRequest));
    }

    private static List<NameValuePair> parseQuery(RequestLine requestLine) {
        fullPath = requestLine.getPathToResource().split("\\?");
        queryParams = URLEncodedUtils.parse(fullPath[1], StandardCharsets.UTF_8);
        return queryParams;
    }


    public static Map<String, List<Part>> parseBodyWithFiles(RequestContext requestContext) {
        FileUploadImpl fileUpload = new FileUploadImpl();
        Map<String, List<Part>> parts = new HashMap<>();
        try {
            FileItemIterator iterStream = fileUpload.getItemIterator(requestContext);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                Part part;
                if (!item.isFormField()) {
                    byte[] content = stream.readAllBytes();
                    part = new Part(false, content);
                } else {
                    String value = Streams.asString(stream);
                    part = new Part(true, value.getBytes());
                }
                List<Part> listParts = parts
                        .computeIfAbsent(name, k -> new ArrayList<>());
                listParts.add(part);
            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();

        }
//            }
        return parts;
    }


    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    String[] getFullPath() {
        return fullPath;
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

    public List<NameValuePair> getBody() {
        return bodyParams;
    }

    @Override
    public String getCharacterEncoding() {
        if (this.getContentType() == null) {
            return null;
        } else {
            return Arrays.stream(this.getContentType().split(";"))
                    .filter(o -> o.startsWith("charset"))
                    .map(o -> o.substring(o.indexOf("=")))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestMethod=" + requestLine.getMethod() +
                ", requestPath=" + requestLine.getPathToResource() +
                ", requestVersion=" + requestLine.getVersionOfProtocol() +
                ", headers=" + headers +
                ", queryParams='" + body + '\'' +
                '}';
    }

    @Override
    public long contentLength() {
        return 0;
    }
}

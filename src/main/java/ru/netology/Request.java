package ru.netology;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.util.Streams;
import org.apache.http.*;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request implements UploadContext {
    private final RequestLine requestLine;
    private final List<String> headers;
    private final String body;
    private final BufferedInputStream in;
    public static final String GET = "GET";
    public static final String POST = "POST";
    private static final List<String> allowedMethods = List.of(GET, POST);
    private String[] pathParts = new String[2];

    public Request(RequestLine requestLine, List<String> headers,
                   String body, BufferedInputStream in) {

        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
        this.in = in;
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
        RequestLine lineOfRequest = new RequestLine(method, pathOfRequestLine, requestLine[2]);
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
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
// вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                final var bodyInLine = new String(bodyBytes);
                return new Request(lineOfRequest, headers, bodyInLine, in);
            }
        }
        return new Request(lineOfRequest, headers, null, in);
    }

    public RequestParser parser(Request request, BufferedInputStream in) {
        try {
            in.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final var queryParams = parseQuery(request.requestLine);
        final var bodyPostParams = parseBody(request);
        if (!request.requestLine.getMethod().equals(GET)) {
            final var contentTypeOptional = extractHeader(headers, "Content-Type");
            if (contentTypeOptional.isPresent()) {
                final var contentType = contentTypeOptional.get();
                if (contentType.startsWith("application/x-www-form-urlencoded")) {
                    return new RequestParser(request, queryParams, bodyPostParams);
                } else if (contentType.startsWith("multipart/form-data")) {
                    final var bodyMultiParts = parseBodyWithFiles(request);
                    return new RequestParser(request, queryParams, bodyPostParams, bodyMultiParts);
                }
                return new RequestParser(request, queryParams, bodyPostParams);
            }

            return new RequestParser(request, queryParams, bodyPostParams);
        }
        return new RequestParser(request, queryParams, null);
    }

    private List<NameValuePair> parseQuery(RequestLine requestLine) {
        final var pathParts = requestLine.getPathToResource().split("\\?");
        if (pathParts.length > 1) {
            return URLEncodedUtils.parse(pathParts[1], StandardCharsets.UTF_8);
        }
        return new ArrayList<>();
    }

    private List<NameValuePair> parseBody(Request request) {
        return URLEncodedUtils.parse(request.body, StandardCharsets.UTF_8);
    }

    private Map<String, List<Part>> parseBodyWithFiles(Request request) {
        FileUploadImpl fileUpload = new FileUploadImpl();
        Map<String, List<Part>> parts = new HashMap<>();
        try {
            FileItemIterator iterStream = fileUpload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                Part part;
                if (!item.isFormField()) {
                    byte[] content = stream.readAllBytes();
                    part = new Part(item.isFormField(), content);
                } else {
                    String value = Streams.asString(stream);
                    part = new Part(item.isFormField(), value);
                }
                List<Part> listParts = parts
                        .computeIfAbsent(name, k -> new ArrayList<>());
                listParts.add(part);
            }
        } catch (FileUploadException | IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
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

    public RequestLine getRequestLine() {
        return this.requestLine;
    }

    @Override
    public String getCharacterEncoding() {
        return Charset.defaultCharset().toString();
    }

    public String getHeader(String headerName) {
        var optionalHeader = extractHeader(headers, headerName);
        return optionalHeader.orElse(null);
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }

    @Override
    public InputStream getInputStream() {
        return in;
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
        return getContentLength();
    }
}

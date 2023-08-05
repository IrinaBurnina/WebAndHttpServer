package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    public RequestLine requestLine;
    public List<String> headers;
    public String body;
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final List<String> allowedMethods = List.of(GET, POST);
    List<NameValuePair> listQueryParams = new ArrayList<>();
    List<NameValuePair> listPostParams = new ArrayList<>();

    public Request(RequestLine requestLine, List<String> headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
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
        String bodyWithParams = null;
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
// вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                bodyWithParams = new String(bodyBytes);

            }
        }
        return new Request(new RequestLine(requestLine[0], requestLine[1], requestLine[2]), headers, bodyWithParams);
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
        return this.requestLine.getPathToResource().split("\\?");
    }

    public List<NameValuePair> getQueryParam(String query) {
        List<NameValuePair> list = new ArrayList<>();
        for (NameValuePair nameValue : listQueryParams) {
            if (query.equals(nameValue.getName())) {
                list.add(nameValue);
            }
        }
        return list;
    }

    public List<NameValuePair> getQueryParams() {
        listQueryParams = URLEncodedUtils.parse(getFullPath()[1], StandardCharsets.UTF_8);
        return listQueryParams;
    }

    public List<NameValuePair> getPostParam(String body) {
        List<NameValuePair> list = new ArrayList<>();
        for (NameValuePair nameValue : listPostParams) {
            if (body.equals(nameValue.getName())) {
                list.add(nameValue);
            }
        }
        return list;
    }

    public List<NameValuePair> getPostParams() {
        if (this.requestLine.getMethod().equals("POST") && body != null) {
            listPostParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
            return listPostParams;
        }
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
}

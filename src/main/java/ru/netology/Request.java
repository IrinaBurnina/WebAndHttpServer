package ru.netology;

import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Request {
    public RequestLine requestLine;
    public List<String> headers;
    public String queryParams;

    public Request(RequestLine requestLine, List<String> headers, String queryParams) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.queryParams = queryParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return HandlerConnections.nameValuePairMap.get(name);
    }

    public Map<String, List<NameValuePair>> getQueryParams() {
        return HandlerConnections.nameValuePairMap;
    }
}

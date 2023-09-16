package ru.netology;

import org.apache.http.NameValuePair;

import java.util.*;

public class RequestParser {
    private final Request request;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> bodyPostParams;
    private final Map<String, List<Part>> bodyMultiParts;
    private final String[] path = new String[2];

    public RequestParser(Request request, List<NameValuePair> queryParams, List<NameValuePair> bodyPostParams,
                         Map<String, List<Part>> bodyMultiParts) {
        this.request = request;
        this.queryParams = queryParams;
        this.bodyPostParams = bodyPostParams;
        this.bodyMultiParts = bodyMultiParts;
    }

    public RequestParser(Request request, List<NameValuePair> queryParams, List<NameValuePair> bodyPostParams) {
        this(request, queryParams, bodyPostParams, null);
    }

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
        return bodyPostParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return getValueParamByName(name, queryParams);
    }

    public List<NameValuePair> getPostParam(String name) {
        return getValueParamByName(name, bodyPostParams);
    }

    public List<Part> getPart(String name) {
        return Collections.unmodifiableList((bodyMultiParts.getOrDefault("name", new ArrayList<>())));
    }

    public Map<String, List<Part>> getParts() {
        return Collections.unmodifiableMap(bodyMultiParts);
    }

    @Override
    public String toString() {
        return "RequestParser{" + "\n" +
                "request=" + "\n" + request + "\n" +
                ", queryParams=" + "\n" + queryParams + "\n" +
                ", bodyPostParams=" + "\n" + bodyPostParams + "\n" +
                ", bodyMultiParts=" + "\n" + bodyMultiParts + "\n" +
                '}';
    }
}

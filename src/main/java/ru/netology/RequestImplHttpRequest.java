package ru.netology;

import org.apache.http.RequestLine;
import org.apache.http.*;
import org.apache.http.params.HttpParams;

public class RequestImplHttpRequest implements HttpRequest {
    public RequestImplHttpRequest() {
    }

    @Override
    public RequestLine getRequestLine() {
        return Request.requestLine;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return Request.requestLine.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(String s) {
        if (s.contains(":")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Header[] getHeaders(String s) {
//        Header[] header = new Header[headers.size()];
//        for (int i = 0; i < header.length; i++) {
//            header[i] = (Header) URLEncodedUtils.parse(headers.get(i), StandardCharsets.UTF_8);
//        }
//        return header;
        return new Header[0];
    }

    @Override
    public Header getFirstHeader(String s) {
        return null;
    }

    @Override
    public Header getLastHeader(String s) {
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        return new Header[0];
    }

    @Override
    public void addHeader(Header header) {

    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public void setHeader(Header header) {

    }

    @Override
    public void setHeader(String s, String s1) {

    }

    @Override
    public void setHeaders(Header[] headers) {

    }

    @Override
    public void removeHeader(Header header) {

    }

    @Override
    public void removeHeaders(String s) {

    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String s) {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(HttpParams httpParams) {

    }
}

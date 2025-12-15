package com.ownwn.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Request {
    private static final Pattern formBoundaryPattern = Pattern.compile("^multipart/form-data; +boundary=(.+)$");

    private final InetSocketAddress remoteAddress;
    private final InputStream requestBody;
    private final Headers requestHeaders;
    private final OutputStream responseBody;
    private final String path;
    private final Map<String, String> cookies;
    private final Map<String, String> queryParameters;
    private String multiPartFormBoundary = null;
    private Map<String, List<FormAttachment>> formData = null;



    public Request(InetSocketAddress remoteAddress, InputStream requestBody, Headers requestHeaders,
                   OutputStream responseBody, String path, Map<String, String> cookies, Map<String, String> queryParameters) {
        this.remoteAddress = remoteAddress;
        this.requestBody = requestBody;
        this.requestHeaders = requestHeaders;
        this.responseBody = responseBody;
        this.path = path;
        this.cookies = cookies;
        this.queryParameters = queryParameters;

        multiPartFormBoundary = tryParseMultiPartFormBoundary();

    }

    public Map<String, List<FormAttachment>> loadFormData() {
        if (multiPartFormBoundary == null) return null;
        if (formData != null) return formData;

        FormByteParser parser = new FormByteParser(requestBody, multiPartFormBoundary);
        return formData = parser.getTypeGroups();
    }

    private String tryParseMultiPartFormBoundary() {
        String contentType = requestHeaders.get("Content-Type");
        if (contentType == null) return null;

        Matcher formBoundaryMatcher = formBoundaryPattern.matcher(contentType);
        if (!formBoundaryMatcher.find()) {
            return null;
        }

        return "--" + formBoundaryMatcher.group(1);
    }


    public static Request createFromExchange(HttpExchange exchange, String basePath) {
        String path = exchange.getRequestURI().getPath();
        if (!path.startsWith(basePath)) {
            throw new RuntimeException("URI did not start with base path!");
        }

        String URI = path.substring(basePath.length()).replaceFirst("/$", "");
        Map<String, String> cookies = parseCookies(new Headers(exchange.getRequestHeaders()));
        Map<String, String> queries = parseQueries(exchange.getRequestURI().getQuery());
        return new Request(exchange.getRemoteAddress(), exchange.getRequestBody(), new Headers(exchange.getRequestHeaders()), exchange.getResponseBody(), URI, cookies, queries);
    }

    private static Map<String, String> parseQueries(String query) {
        if (query == null || query.isBlank()) return Map.of();

        Map<String, String> queries = new HashMap<>();
        String[] parts = query.split("&");
        for (String pair : parts) {
            String[] keyValue = pair.split("=");
            if (keyValue.length != 2) continue;
            if (keyValue[1].isBlank() || keyValue[0].isBlank()) continue;
            queries.put(keyValue[0], keyValue[1]);
        }
        return queries;
    }

    private static Map<String, String> parseCookies(Headers headers) {
        String cookieString = headers.get("Cookie");
        if (cookieString == null) {
            return Map.of();
        }

        String[] cookiePairs = cookieString.split(";", 50);
        if (cookiePairs.length >= 50) {
            System.err.println("Max number of cookies reached! " + cookiePairs[0] + ", " + cookiePairs[1] + "...");
            return Map.of();
        }

        Map<String, String> cookies = new HashMap<>();

        for (String cookie : cookiePairs) {
            String[] parts = cookie.split("=", 2);
            if (parts.length != 2) {
                System.err.println("Malformed cookie recieved " + cookie.substring(0, Math.min(20, cookie.length())));
                return cookies;
            }
            cookies.put(parts[0].trim(), parts[1]);
        }
        return cookies;
    }

    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    public InputStream requestBody() {
        return requestBody;
    }

    public Headers requestHeaders() {
        return requestHeaders;
    }

    public OutputStream responseBody() {
        return responseBody;
    }

    public String path() {
        return path;
    }

    public Map<String, String> cookies() {
        return cookies;
    }

    public Map<String, String> queryParameters() {
        return queryParameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Request) obj;
        return Objects.equals(this.remoteAddress, that.remoteAddress) &&
                Objects.equals(this.requestBody, that.requestBody) &&
                Objects.equals(this.requestHeaders, that.requestHeaders) &&
                Objects.equals(this.responseBody, that.responseBody) &&
                Objects.equals(this.path, that.path) &&
                Objects.equals(this.cookies, that.cookies) &&
                Objects.equals(this.queryParameters, that.queryParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remoteAddress, requestBody, requestHeaders, responseBody, path, cookies, queryParameters);
    }

    @Override
    public String toString() {
        return "Request[" +
                "remoteAddress=" + remoteAddress + ", " +
                "requestBody=" + requestBody + ", " +
                "requestHeaders=" + requestHeaders + ", " +
                "responseBody=" + responseBody + ", " +
                "path=" + path + ", " +
                "cookies=" + cookies + ", " +
                "queryParameters=" + queryParameters + ']';
    }


}

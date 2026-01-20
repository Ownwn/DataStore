package com.ownwn.server;


import com.ownwn.server.java.lang.replacement.*;

import java.util.Arrays;
import java.util.Locale;

public class Headers {
    private final Map<String, String> internalHeaders;

    public Headers(com.sun.net.httpserver.Headers headers) {
        this();
        for (var header : headers.entrySet()) {
            internalHeaders.put(header.getKey(), header.getValue().getFirst());
        }
    }

    public static Headers fromRawList(List<String> rawHeaders) {
        Headers headers = new Headers();
        for (String header : rawHeaders) {
            if (header == null) continue;
            String[] parts = header.split(":");
            if (parts.length != 2) continue;
            headers.put(parts[0], parts[1]);
        }
        return headers;
    }

    public Map<String, String> getAndRemoveCookies() {
        String cookieValue = internalHeaders.remove("Cookie");
        if (cookieValue == null || cookieValue.isBlank()) return Map.of();
        Map<String, String> res = new HashMap<>();
        String[] cookies = cookieValue.split(";"); // todo breaks on ; inside cookie content?
        for (String cookie : cookies) {
            String[] parts = cookie.split("=");
            if (parts.length != 2) continue;
            res.put(parts[0], parts[1]);
        }
        return res;
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return internalHeaders.entrySet();
    }

    public Headers() {
        internalHeaders = new HashMap<>();
    }

    public String put(String headerName, String headerValue) {
        return internalHeaders.put(capitalise(headerName), headerValue);
    }

    public String get(String headerName) {
        return internalHeaders.get(capitalise(headerName));
    }

    @Override
    public String toString() {
        return "Headers{" +
                "internalHeaders=" + internalHeaders +
                '}';
    }

    private String capitalise(String s) {
        return Arrays.stream(s.split("-"))
                .map(str -> (str.charAt(0) + "").toUpperCase(Locale.ROOT) + str.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining("-"));
    }


}

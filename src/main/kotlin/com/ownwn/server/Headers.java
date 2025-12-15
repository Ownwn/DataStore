package com.ownwn.server;

import java.util.*;
import java.util.stream.Collectors;

public final class Headers {
    private final Map<String, String> internalHeaders;

    public Headers(com.sun.net.httpserver.Headers headers) {
        this();
        for (var header : headers.entrySet()) {
            internalHeaders.put(header.getKey(), header.getValue().get(0));
        }
    }

    public Map<String, List<String>> tempBridge() {
        Map<String, List<String>> res = new HashMap<>();
        for (var header : internalHeaders.entrySet()) {
            res.put(header.getKey(), List.of(header.getValue()));

        }
        return res;
    }

    public Headers() {
        internalHeaders = new HashMap<>();
    }

    public String put(String headerName, String headerValue) {
        return internalHeaders.put(capitalise(headerName), capitalise(headerValue));
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

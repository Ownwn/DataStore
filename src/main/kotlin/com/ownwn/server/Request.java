package com.ownwn.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public record Request(InetSocketAddress remoteAddress, InputStream requestBody, Headers requestHeaders,
                      OutputStream responseBody, String path, Map<String, String> cookies) {

    public static Request createFromExchange(HttpExchange exchange) {
        String URI = exchange.getRequestURI().getPath().replaceFirst("/$", "");
        Map<String, String> cookies = parseCookies(exchange.getRequestHeaders());
        return new Request(exchange.getRemoteAddress(), exchange.getRequestBody(), exchange.getRequestHeaders(), exchange.getResponseBody(), URI, cookies);
    }

    private static Map<String, String> parseCookies(Headers headers) {
        String cookieString = headers.getFirst("Cookie");
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


}

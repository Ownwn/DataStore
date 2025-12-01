package com.ownwn.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public record Request(InetSocketAddress remoteAddress, InputStream requestBody, Headers requestHeaders,
                      OutputStream responseBody) {

    public static Request createFromExchange(HttpExchange exchange) {
        return new Request(exchange.getRemoteAddress(), exchange.getRequestBody(), exchange.getRequestHeaders(), exchange.getResponseBody());
    }
}

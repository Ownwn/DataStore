package com.ownwn.server;

import com.sun.net.httpserver.Headers;
import kotlin.text.Charsets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public record Response(int status, InputStream inputStream, Headers headers) {
    public static Response of(int status, String body) {
        return Response.of(status, body.getBytes(Charsets.UTF_8));
    }

    public byte[] body() throws IOException {
        inputStream.mark(Integer.MAX_VALUE); // todo could improve
        byte[] res = inputStream.readAllBytes();
        inputStream.reset();
        return res;
    }

    public static Response of(int status, byte[] body, Map<String, String> headers) {
        Headers resHeaders = new Headers();
        for (var header : headers.entrySet()) {
            resHeaders.put(header.getKey(), List.of(header.getValue()));
        }
        return new Response(status, new ByteArrayInputStream(body), resHeaders);
    }

    public static Response ok(String body) {
        return Response.of(200, body);
    }

    public static Response of(int status, byte[] body) {
        return Response.of(status, body, Map.of());
    }

    public static Response ok(byte[] body) {
        return Response.of(200, body);
    }

    public static Response softRedirect(String path) {
        return Response.of(302, new byte[]{}, Map.of("Location", path));
    }
}
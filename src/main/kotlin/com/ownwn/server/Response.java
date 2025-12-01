package com.ownwn.server;

public record Response(int status, byte[] body) {
    public static Response of(int status, String body) {
        return new Response(status, body.getBytes());
    }

    public static Response ok(String body) {
        return new Response(200, body.getBytes());
    }

    public static Response of(int status, byte[] body) {
        return new Response(status, body);
    }

    public static Response ok(byte[] body) {
        return new Response(200, body);
    }
}
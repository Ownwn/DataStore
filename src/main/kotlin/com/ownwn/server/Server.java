package com.ownwn.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class Server {
    private final Map<String, RequestHandler> methods;
    private static final String host = "localhost";

    public static void create(int port) {
        try {
            String packageName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass()
                    .getPackageName();
            new Server(packageName, port);
            System.out.println("Server started at http://" + host + ":" + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Server(String packageName, int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        methods = AnnotationFinder.getAllAnnotatedMethods(packageName);

        server.createContext("/").setHandler(exchange -> {
            try {
                handle(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        String url = cleanUrl(exchange.getRequestURI().getPath());
        RequestHandler handler = methods.get(url);

        if (handler == null) {
            exchange.sendResponseHeaders(404, "404".length());
            exchange.getResponseBody().write("404".getBytes());
            exchange.getResponseBody().close();
            return;
        }

        if (!handler.method().name().equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
        }

        Request request = Request.createFromExchange(exchange);
        Response response = handler.handle(request);

        exchange.sendResponseHeaders(response.status(), response.body().length);
        exchange.getResponseBody().write(response.body());
        exchange.getResponseBody().close();
    }

    private String cleanUrl(String url) {
        if (url.startsWith("/")) url = url.substring(1);
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }
}
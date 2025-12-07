package com.ownwn.server;

import com.ownwn.server.intercept.InterceptReciever;
import com.ownwn.server.intercept.Interceptor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private final Map<String, RequestHandler> handleMethods = new HashMap<>();
    private final List<Interceptor> interceptMethods = new ArrayList<>();
    private final String friendlyAddress;

    public static void create(String hostName, int port) {
        try {
            String packageName = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass()
                    .getPackageName();
            Server s = new Server(packageName, hostName, port);
            System.out.println("Server started at " + s.friendlyAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Server(String packageName, String hostName, int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(hostName, port), 0);
        AnnotationFinder.loadAllAnnotatedMethods(packageName, handleMethods, interceptMethods);

        server.createContext("/").setHandler(exchange -> {
            try {
                handle(exchange);
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        });

        server.start();

        friendlyAddress = server.getAddress().toString().replaceFirst("^/", "");
    }

    private void handle(HttpExchange exchange) throws IOException {

        Request request = Request.createFromExchange(exchange);

        for (Interceptor interceptor : interceptMethods) {
            InterceptReciever rec = new InterceptReciever();
            interceptor.handle(request, rec);

            if (rec.isClosed()) {
                exchange.sendResponseHeaders(rec.getResponse().status(), rec.getResponse().body().length);
                exchange.getResponseBody().write(rec.getResponse().body());
                exchange.getResponseBody().close();
                return;
            }
        }

        String url = cleanUrl(exchange.getRequestURI().getPath());
        RequestHandler handler = handleMethods.get(url);

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
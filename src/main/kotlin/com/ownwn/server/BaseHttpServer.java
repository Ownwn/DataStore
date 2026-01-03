package com.ownwn.server;

import com.ownwn.server.request.GetRequest;
import com.ownwn.server.request.PostRequest;
import com.ownwn.server.request.Request;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseHttpServer {
    private final Pattern getRequestParamPattern = Pattern.compile("[?&]([^?=&]+)=([^?=&]+)"); // todo single responsibility principal?

    private final int port;
    private final Consumer<Request> handler;
    ServerSocket socket;

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public static BaseHttpServer create(int port, Consumer<Request> handler) {
        return new BaseHttpServer(port, handler);
    }


    private BaseHttpServer(int port, Consumer<Request> handler) {
        this.port = port;
        this.handler = handler;

        new Thread(() -> {
            try {
                socket = new ServerSocket(port);
                while (true) {
                    Socket client;
                    try {
                        client = socket.accept();
                        client.setSoTimeout(5000);
                    } catch (SocketTimeoutException socketTimeoutException) {
                        continue;
                    }

                    new Thread(() -> {
                        Request request = createRequest(client);
                        handler.accept(request);
                    }).start();
                }
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }

    private Request createRequest(Socket client) {
        try {
            InputStream s = client.getInputStream();

            List<String> rawHeaders = getRawHeaders(s);
            String requestMethod = rawHeaders.removeFirst();
            String[] requestParts = requestMethod.split(" ");
            HttpMethod method = HttpMethod.fromString(requestParts[0]);
            if (!requestMethod.endsWith("HTTP/1.1") || requestParts.length != 3 || method == null) {
                // todo malformed response
                return null;
            }

            String requestPath = requestParts[1];
            Headers headers = Headers.fromRawList(rawHeaders);
            Map<String, String> cookies = headers.getAndRemoveCookies();
            StringBuilder newPath = new StringBuilder(requestPath);
            Map<String, String> requestParams = getGetRequestParamsAndMutatePath(newPath);
            requestPath = newPath.toString();



            if (method == HttpMethod.GET) {
                return new GetRequest(client.getInetAddress(), s, headers, client.getOutputStream(), requestPath, cookies, requestParams);
            } else if (method == HttpMethod.POST) {
                return new PostRequest(client.getInetAddress(), s, headers, client.getOutputStream(), requestPath, cookies, requestParams);
            } else {
                return new GetRequest(client.getInetAddress(), s, headers, client.getOutputStream(), requestPath, cookies, requestParams);
                // todo
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Mutates {@code path} */
    private Map<String, String> getGetRequestParamsAndMutatePath(StringBuilder path) {
        if (path.isEmpty() || path.toString().equals("/")) return Map.of();
        Map<String, String> params = new HashMap<>();

        int endOfPath = -1;
        Matcher m = getRequestParamPattern.matcher(path);
        while (m.find()) {
            if (endOfPath == -1) endOfPath = m.start();
            params.put(m.group(1), m.group(2));
        }
        if (endOfPath != -1) {
            path.delete(endOfPath, path.length());
        }
        return params;
    }

    private List<String> getRawHeaders(InputStream inputStream) throws IOException {
        List<String> headers = new ArrayList<>();
        LineState state = LineState.NONE;
        int b;
        StringBuilder currentHeaderRaw = new StringBuilder(32);

        while ((b = inputStream.read()) != -1) { // todo limit header length to reasonable strlen?
            if (b == 13) {
                if (state == LineState.NONE) state = LineState.SEEN_CR1;
                else if (state == LineState.SEEN_LF1) state = LineState.SEEN_CR2;
            } else if (b == 10) {
                if (state == LineState.SEEN_CR1) {
                    headers.add(currentHeaderRaw.substring(0, currentHeaderRaw.length()-1));
                    currentHeaderRaw = new StringBuilder(32);
                    state = LineState.SEEN_LF1;
                    continue;
                } else if (state == LineState.SEEN_CR2) {
                    return headers;
                }
            } else {
                state = LineState.NONE;
            }
            currentHeaderRaw.append((char) b);
        }


        return headers;
    }

    private String getRequestMethod(InputStream inputStream) throws IOException {
        LineState state = LineState.NONE;
        int b;
//        byte[] bob = inputStream.readAllBytes();
        StringBuilder method = new StringBuilder(1000); // todo bad?

        while ((b = inputStream.read()) != -1) {
            if (b == 10 && state == LineState.SEEN_CR1) { // new line
                return method.toString();
            }
            if (b == 13) {
                state = LineState.SEEN_CR1;
            }
            method.append((char) b);

            if (method.length() > 50) {
                break;
            }
        }
        return null; // todo malformed;

    }

    enum LineState {
        NONE, SEEN_CR1, SEEN_LF1, SEEN_CR2
    }

    record Pair<A,B>(A a, B b) {}
}

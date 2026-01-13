package com.ownwn.server;

import com.ownwn.server.request.GetRequest;
import com.ownwn.server.request.PostRequest;
import com.ownwn.server.request.Request;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.foreign.ValueLayout.*;

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

    /** adapted from https://dev.java/learn/ffm/native/ */
    private static void invokeStrdup(String pattern) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            FFIHelper ffiHelper = new FFIHelper(arena);


            // Allocate off-heap memory and
            // copy the argument, a Java string, into off-heap memory
            MemorySegment nativeString = arena.allocateFrom(pattern);

            // Obtain an instance of the native linker
            Linker linker = Linker.nativeLinker();

            // Locate the address of the C function signature
            SymbolLookup stdLib = linker.defaultLookup();
            MemorySegment strdup_addr = stdLib.find("accept").get();

            // Create a description of the C function
            var layout = MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE);
            FunctionDescriptor strdup_sig = FunctionDescriptor.of(
                    ValueLayout.ADDRESS.withTargetLayout(layout),
                    ValueLayout.ADDRESS.withTargetLayout(layout),
                    ValueLayout.ADDRESS.withTargetLayout(layout),
                    ValueLayout.ADDRESS.withTargetLayout(layout)
            );

            // Create a downcall handle for the C function
            var strdup_handle = linker.downcallHandle(strdup_addr, strdup_sig);

            // Call the C function directly from Java
            MemorySegment duplicatedAddress = (MemorySegment) strdup_handle.invokeExact(nativeString);

            for (int i = pattern.length() - 1; i >= 0; i--) {
                IO.println(duplicatedAddress.getString(i));
            }
        }
    }


    private static void setupWithArena() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {


            int socketHandle = (int) callIntFunction(arena, "socket", List.of(2, 1, 0));
            if (socketHandle < 0) throw new RuntimeException("Bad socket");

            System.out.println("fd " + socketHandle);
            MemorySegment opt = arena.allocateFrom(JAVA_INT, 1);
            System.out.println("opt start " + opt.get(JAVA_INT, 0));
            callLongFunction(arena, "setsockopt", List.of((int) socketHandle, 1, 2, opt.address(), 4)); // todo sizeof int 4 bytes?
            System.out.println("opt after " + opt.get(JAVA_INT, 0));

            MemorySegment sockaddr_in = arena.allocate(16L, 1);
            // 2 and 4 u16 and u32

            // family 2, port 2, addr 4, zerof 8
            sockaddr_in.set(JAVA_SHORT, 0, (short) 2);
            sockaddr_in.set(JAVA_SHORT, 2, (short) callIntFunction(arena, "htons", List.of(8081)));
            sockaddr_in.set(JAVA_INT, 4, 0);
            sockaddr_in.set(JAVA_LONG, 8, 0);

            Linker linker = Linker.nativeLinker();

            SymbolLookup stdLib = linker.defaultLookup();
            MemorySegment function_addr = stdLib.find("bind").get();

            FunctionDescriptor bindFd = FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_LONG);

            var bindMh = linker.downcallHandle(function_addr, bindFd);
            if ((long) bindMh.invoke(socketHandle, sockaddr_in, 16L) < 0) {
                throw new RuntimeException("Failed to invoke bind");
            }

            FunctionDescriptor listenFd  = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT);

            var listenMh = linker.downcallHandle(stdLib.find("listen").get(), listenFd);
            if ((long) listenMh.invoke(socketHandle, 16) < 0) {
                throw new RuntimeException("Failed to invoke listen");
            }

            for (;;) {

                FunctionDescriptor acceptFd = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT);

                var acceptMh = linker.downcallHandle(stdLib.find("accept").get(), acceptFd);
                int c = (int) ((long) acceptMh.invoke(socketHandle, 0, 0));
                if (c < 0) {
                    System.err.println("Failed to invoke accept");
                    continue;
                }
                System.out.println("happy!");

                MemorySegment buf = arena.allocate(1000, 1L);

                FunctionDescriptor readFd = FunctionDescriptor.of(JAVA_LONG, JAVA_INT, ADDRESS, JAVA_LONG);

                var readMh = linker.downcallHandle(stdLib.find("read").get(), readFd);
                int numReaded = (int) ((long) readMh.invoke(c, buf, buf.byteSize()));

                if (numReaded > 0) {
                    System.out.println("read successful!");
                }


                FunctionDescriptor writeFd = FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_LONG);

                var writeMh = linker.downcallHandle(stdLib.find("write").get(), writeFd);

                MemorySegment resString = arena.allocateFrom("cool res foobar");

                writeMh.invoke(c, resString, resString.byteSize());

                System.out.println("msg from client: " + buf.getString(0, StandardCharsets.UTF_8));

                callIntFunction(arena, "close", List.of(c));
            }




        }
    }

    /** adapted from https://dev.java/learn/ffm/native/ */
    private static long callIntFunction(Arena arena, String name, List<Integer> args) throws Throwable {
        int numArgs = args.size();

        List<MemorySegment> argSegments = new ArrayList<>(numArgs);

        for (int arg : args) {
            argSegments.add(arena.allocateFrom(JAVA_INT, arg));
        }

        Linker linker = Linker.nativeLinker();

        SymbolLookup stdLib = linker.defaultLookup();
        MemorySegment function_addr = stdLib.find(name).get();

        MemoryLayout layouts[] = new MemoryLayout[numArgs];
        Arrays.fill(layouts, JAVA_INT);

        FunctionDescriptor fd = FunctionDescriptor.of(JAVA_INT, layouts);

        var mh = linker.downcallHandle(function_addr, fd);
        return (int) mh.invokeWithArguments(args);
    }

    /** adapted from https://dev.java/learn/ffm/native/ */
    private static long callLongFunction(Arena arena, String name, List<Object> args) throws Throwable {
        int numArgs = args.size();

        List<MemorySegment> argSegments = new ArrayList<>(numArgs);

        for (Object arg : args) {
            if (arg instanceof Long l) {
                argSegments.add(arena.allocateFrom(JAVA_LONG, l));
            } else {
                argSegments.add(arena.allocateFrom(JAVA_INT, (int) arg));
            }
        }

        Linker linker = Linker.nativeLinker();

        SymbolLookup stdLib = linker.defaultLookup();
        MemorySegment function_addr = stdLib.find(name).get();

        MemoryLayout layouts[] = new MemoryLayout[numArgs];
        Arrays.fill(layouts, JAVA_LONG);

        FunctionDescriptor fd = FunctionDescriptor.of(JAVA_LONG, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_LONG, JAVA_LONG);

        var mh = linker.downcallHandle(function_addr, fd);
        return (long) mh.invokeWithArguments(args);
    }


    private BaseHttpServer(int port, Consumer<Request> handler) {
        this.port = port;
        this.handler = handler;

        try {
            setupWithArena();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

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

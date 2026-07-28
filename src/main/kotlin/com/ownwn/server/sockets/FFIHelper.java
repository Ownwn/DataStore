package com.ownwn.server.sockets;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import com.ownwn.server.java.lang.replacement.ArrayList;
import com.ownwn.server.java.lang.replacement.HashMap;
import com.ownwn.server.java.lang.replacement.List;
import com.ownwn.server.java.lang.replacement.Map;
import com.ownwn.server.java.lang.replacement.stream.IntStream;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

public class FFIHelper {
    private static Linker linker;
    private static SymbolLookup stdLib;
    private static MethodHandle sendFunctionHandle;
    private static final Map<String, MethodHandle> methodCache = new HashMap<>();

    static {
        linker = Linker.nativeLinker();
        stdLib = linker.defaultLookup();
    }

    /** You must close the arena yourself! */
    public FFIHelper() {
    }

    public static FFIHelper of() {
        return new FFIHelper();
    }

//    public long sendNative(int fd, MemorySegment buf, int length) {
//
//    }

    private <T extends MemoryLayout> MethodHandle loadMethodHandle(String name, T returnType, List<T> types) {
        return methodCache.computeIfAbsent(name, _ -> {
            MemorySegment function_addr = stdLib.find(name).orElseThrow(() -> new RuntimeException("Can't find function of name " + name));
            FunctionDescriptor fd = FunctionDescriptor.of(returnType, types.toArray(new MemoryLayout[types.size()]));
            return linker.downcallHandle(function_addr, fd);
        });
    }

    /** adapted from https://dev.java/learn/ffm/native/ */
    public <T extends MemoryLayout> Object callFunction(String name, T returnType, List<T> types, List<Object> args) throws Throwable {
        if (types.size() != args.size()) {
            throw new IllegalArgumentException("Mismatch of number of args and types!");
        }

        MethodHandle methodHandle = loadMethodHandle(name, returnType, types);
        return methodHandle.invokeWithArguments(args.toArray(new Object[0]));
    }

    public <T extends MemoryLayout> Object callIntFunction(String name, T returnType, List<Integer> args) throws Throwable { // todo unnecessary copy constructor
        return callFunction(name, returnType, new ArrayList<>(IntStream.range(0, args.size()).mapToObj(ignored -> (MemoryLayout) JAVA_INT).toList()), new ArrayList<>(args.stream().map(i -> (Object) i).toList()));
    }

    public <T extends MemoryLayout> Object callShortFunction(String name, T returnType, List<Short> args) throws Throwable {
        return callFunction(name, returnType, new ArrayList<>(IntStream.range(0, args.size()).mapToObj(ignored -> (MemoryLayout) JAVA_SHORT).toList()), new ArrayList<>(args.stream().map(i -> (Object) i).toList()));
    }
}

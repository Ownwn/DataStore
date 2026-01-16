package com.ownwn.server.sockets;

import java.lang.foreign.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

public class FFIHelper {
    private Arena arena;
    private Linker linker;
    private SymbolLookup stdLib;

    /** You must close the arena yourself! */
    public FFIHelper(Arena arena) {
        this.arena = arena;
        linker = Linker.nativeLinker();
        stdLib = linker.defaultLookup();
    }

    public static FFIHelper ofArena(Arena arena) {
        return new FFIHelper(arena);
    }

    /** adapted from https://dev.java/learn/ffm/native/ */
    public <T extends MemoryLayout> Object callFunction(String name, T returnType, List<T> types, List<Object> args) throws Throwable {
        if (types.size() != args.size()) {
            throw new IllegalArgumentException("Mismatch of number of args and types!");
        }

        MemorySegment function_addr = stdLib.find(name).orElseThrow(() -> new RuntimeException("Can't find function of name " + name));

        FunctionDescriptor fd = FunctionDescriptor.of(returnType, types.toArray(new MemoryLayout[types.size()]));

        var mh = linker.downcallHandle(function_addr, fd);
        return mh.invokeWithArguments(args);
    }

    public <T extends MemoryLayout> Object callIntFunction(String name, T returnType, List<Integer> args) throws Throwable {
        return callFunction(name, returnType, IntStream.range(0, args.size()).mapToObj(ignored -> (MemoryLayout) JAVA_INT).toList(), args.stream().map(i -> (Object) i).toList());
    }

    public <T extends MemoryLayout> Object callShortFunction(String name, T returnType, List<Short> args) throws Throwable {
        return callFunction(name, returnType, IntStream.range(0, args.size()).mapToObj(ignored -> (MemoryLayout) JAVA_SHORT).toList(), args.stream().map(i -> (Object) i).toList());
    }
}

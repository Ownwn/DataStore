package com.ownwn.server;

import java.lang.foreign.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.foreign.ValueLayout.JAVA_INT;

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

    /** adapted from https://dev.java/learn/ffm/native/ */
    public Object callFunction(Arena arena, String name, List<MemoryLayout> types, List<Object> args) throws Throwable {
        int numArgs = args.size();
        if (types.size() != numArgs) {
            throw new IllegalArgumentException("Mismatch of number of args and types!");
        }

        MemorySegment function_addr = stdLib.find(name).orElseThrow(() -> new RuntimeException("Can't find function of name " + name));

        FunctionDescriptor fd = FunctionDescriptor.of(types.get(0), types.subList(1, numArgs).toArray(new MemoryLayout[numArgs]));

        var mh = linker.downcallHandle(function_addr, fd);
        return mh.invokeWithArguments(args);
    }
}

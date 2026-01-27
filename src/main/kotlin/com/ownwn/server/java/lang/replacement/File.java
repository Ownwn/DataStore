package com.ownwn.server.java.lang.replacement;

import com.ownwn.server.sockets.FFIHelper;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class File {
    private final String path;

    public File(String path) {
        this.path = path;
    }

    public File[] listFiles() { // todo proper arena shit
        try (Arena a = Arena.ofConfined()) {
            FFIHelper ffiHelper = FFIHelper.of();
            MemorySegment DIR_p = a.allocate(8);
            MemorySegment dirName = a.allocateFrom(path);

            int res = -1;
            try {

                DIR_p = (MemorySegment) ffiHelper.callFunction("opendir", ValueLayout.ADDRESS, List.of(ValueLayout.ADDRESS), List.of(dirName));
                if (DIR_p.equals(MemorySegment.NULL)) {
                    throw new Error("nah");
                }
                MemorySegment dirent_p = a.allocate(280);
                dirent_p = (MemorySegment) ffiHelper.callFunction("readdir", ValueLayout.ADDRESS, List.of(ValueLayout.ADDRESS), List.of(DIR_p));
                MemorySegment good = dirent_p.reinterpret(280);
                System.out.println(good.byteSize());
                for (int i = 0; i < 260; i++) {
                    String s = good.getString(i);
                    if (s.equals(".gitignore")) {
                        System.out.println("git at " + i);
                    }
                }
                String file_name = good.getString(14);
                System.out.println(file_name);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }




            System.out.println("got " + res);
        }
        return null;
    }

}

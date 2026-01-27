package com.ownwn.server.java.lang.replacement;

import com.ownwn.server.sockets.FFIHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public record File(String path) {

    public File[] listFiles() { // todo proper arena shit
        List<File> files = new ArrayList<>();
        try (Arena a = Arena.ofConfined()) {
            FFIHelper ffiHelper = FFIHelper.of();
            MemorySegment DIR_p = a.allocate(8);
            MemorySegment dirName = a.allocateFrom(path);

            try {
                DIR_p = (MemorySegment) ffiHelper.callFunction("opendir", ValueLayout.ADDRESS, List.of(ValueLayout.ADDRESS), List.of(dirName));
                if (DIR_p.equals(MemorySegment.NULL)) {
                    return new File[0];
                }

                MemorySegment dirent_p;

                while (!(dirent_p = (MemorySegment) ffiHelper.callFunction("readdir", ValueLayout.ADDRESS, List.of(ValueLayout.ADDRESS), List.of(DIR_p))).equals(MemorySegment.NULL)) {
                    MemorySegment dirent = dirent_p.reinterpret(280);
                    String fileName = dirent.getString(19);
                    files.add(new File(fileName));
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            return files.toArray(new File[files.size()]);
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "File[" + path + "]";
    }
}

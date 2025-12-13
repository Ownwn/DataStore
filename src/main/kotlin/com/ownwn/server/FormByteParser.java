package com.ownwn.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormByteParser {
    private static final int LINE_FEED = 10;
    private static final Pattern dispositionPattern = Pattern.compile("Content-Disposition: form-data; name=\"([^\"]+)\"(?:; filename=\"([^\"]+)\")?");

    private final InputStream body;
    private final String boundary;
    private int boundaryIndex = 0;
    private List<Chunk> chunks;

    private record Chunk(String disposition, byte[] bytes) {
        @Override
        public String toString() {
            return disposition + "||||EndOfDeposition||||"+ new String(bytes);
        }
    }


    int current;
    public FormByteParser(InputStream body, String boundary) {
        this.body = body;
        this.boundary = boundary;

        List<List<Byte>> rawParts;
        try {
            rawParts = readParts();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        chunks = rawParts.stream()
                .map(l -> l.subList(0, l.size()-boundary.length()))
                .filter(l -> !l.isEmpty())
                .map(l ->{
                    int index = indexOf(l, new byte[]{13, 10,13,10}); // CRLF CRLF
                    if (index == -1) {
                        return null;
                    }
                    return new Chunk(new String(makeArray(l.subList(0, index))), makeArray(l.subList(index+4, l.size()-2)));
                } )
                .toList();
    }

    public Map<String, List<FormAttachment>> getTypeGroups() {
        return chunks.stream().collect(HashMap::new, (map, chunk) -> {

            String[] contentHeaders = chunk.disposition.split("\n");
            Matcher m = dispositionPattern.matcher(contentHeaders[0]);
            if (!m.find()) return; // malformed request

            FormAttachment attachment = new FormAttachment(m.group(1), m.groupCount() >= 2 ? m.group(2) : "text", chunk.bytes());

            map.computeIfAbsent(m.group(1), g -> new ArrayList<>()).add(attachment);

        }, (p1, p2) -> {throw new Error("cant parallel");});
    }

    private int indexOf(List<Byte> haystack, byte[] needle) {
        for (int i = 0; i <= haystack.size()-needle.length; i++) {
            boolean f = true;
            for (int k = 0; k < needle.length; k++) {
                if (haystack.get(i+k) != needle[k]) {
                    f = false;
                    break;
                }
            }
            if (f) return i;

        }
        return -1;
    }

    public List<List<Byte>> readParts() throws IOException {
        List<List<Byte>> parts = new ArrayList<>();
        List<Byte> part = new ArrayList<>(16384); // todo check ram usage

        while ((current = body.read()) != -1) {
            if (boundaryIndex >= boundary.length()) {
                if (!part.isEmpty()) {
                    parts.add(part);
                    part = new ArrayList<>();
                }

                boundaryIndex = 0;
                body.read();
                continue;
            }
            if (current != boundary.charAt(boundaryIndex++)) {
                boundaryIndex = 0;
            }
            part.add((byte) current);

        }
        return parts;
    }

    private static byte[] makeArray(List<Byte> bytes) {
        byte[] res = new byte[bytes.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = bytes.get(i);
        }
        return res;
    }

    public void skipBoundary() throws IOException {
        body.skip(boundary.length());
        body.read();
    }

    public String[] readContentDisposition() throws IOException {
        StringBuilder disposition = new StringBuilder();
        while ((current = body.read()) != LINE_FEED) {
            disposition.append(current);
        }

        return disposition.substring(32).split("; ");
    }
}

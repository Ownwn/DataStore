package com.ownwn.server.response;

import com.sun.net.httpserver.Headers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public abstract class Response {
    protected Headers headers = new Headers();
    protected int status;

    public abstract int bodyLength();
    public abstract InputStream body() throws IOException;


    public Headers headers() {
        return headers;
    }

    public int status() {
        return status;
    }

    public static TemplateResponse notFound = new TemplateResponse(404, TemplateResponse.notFoundUrl) {
        @Override
        public InputStream body() throws IOException {
            Optional<File> errorNotFoundTemplate = getTemplateFile(notFoundUrl);
            if (errorNotFoundTemplate.isPresent()) {
                return Files.newInputStream(errorNotFoundTemplate.get().toPath());
            }

            return new ByteArrayInputStream("<h1>404 Not found</h1>".getBytes(StandardCharsets.UTF_8));

        }
    };
}
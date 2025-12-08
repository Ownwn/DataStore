package com.ownwn.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TemplateManager {
    private static final String templatesDirectory = "templates/";
    public static final String errorTemplate = "notfound";
    final File templatesFolder;

    public TemplateManager() {
        templatesFolder = Path.of(templatesDirectory).toFile();
        if (!templatesFolder.exists() || !templatesFolder.isDirectory()) {
            System.err.println("No templates found. Server cannot serve html files");
        }
    }

    public Optional<File> getTemplateFile(String url) {
        //noinspection DataFlowIssue
        for (File template : templatesFolder.listFiles()) {
            if (template.isDirectory() || !template.getName().endsWith(".html")) {
                continue;
            }
            String name = template.getName().replaceFirst("\\.html$", "");
            if (url.equals(name)) {
                return Optional.of(template);
            }
        }
        return Optional.empty();
    }

    public boolean hasTemplate(String url) {
        return getTemplateFile(url).isPresent();
    }

    public InputStream getTemplateContent(String url) throws IOException {
        Optional<File> file = getTemplateFile(url);
        if (file.isEmpty()) {
            System.err.println("Invalid template!");
            return InputStream.nullInputStream();
        }

        return Files.newInputStream(file.get().toPath());
    }
}

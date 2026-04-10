package com.pch.mng.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LocalBlobStorage implements BlobStorage {

    private final Path baseDir;

    public LocalBlobStorage(Path baseDir) throws IOException {
        this.baseDir = baseDir.toAbsolutePath().normalize();
        Files.createDirectories(this.baseDir);
    }

    @Override
    public String put(String objectKey, InputStream data, long contentLength, String contentType) throws IOException {
        Path target = resolve(objectKey);
        Files.createDirectories(target.getParent());
        Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        return objectKey;
    }

    @Override
    public InputStream get(String objectKey) throws IOException {
        return Files.newInputStream(resolve(objectKey));
    }

    @Override
    public void delete(String objectKey) throws IOException {
        Path p = resolve(objectKey);
        Files.deleteIfExists(p);
    }

    private Path resolve(String objectKey) {
        Path p = baseDir.resolve(objectKey).normalize();
        if (!p.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid object key");
        }
        return p;
    }
}

package com.pch.mng.storage;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

public class S3BlobStorage implements BlobStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3BlobStorage(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String put(String objectKey, InputStream data, long contentLength, String contentType) throws IOException {
        PutObjectRequest.Builder b = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey);
        if (contentType != null && !contentType.isBlank()) {
            b.contentType(contentType);
        }
        s3Client.putObject(b.build(), RequestBody.fromInputStream(data, contentLength));
        return objectKey;
    }

    @Override
    public InputStream get(String objectKey) {
        return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build());
    }

    @Override
    public void delete(String objectKey) throws IOException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
        } catch (RuntimeException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}

package com.pch.mng.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.nio.file.Path;

@Configuration
public class StorageConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "type", havingValue = "s3")
    public S3Client s3Client(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.getS3();
        var builder = S3Client.builder().region(Region.of(s3.getRegion()));
        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3.getEndpoint()))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
            if (s3.getAccessKey() != null && !s3.getAccessKey().isBlank()
                    && s3.getSecretKey() != null && !s3.getSecretKey().isBlank()) {
                builder.credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())));
            }
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    @Bean
    public BlobStorage blobStorage(StorageProperties properties, org.springframework.beans.factory.ObjectProvider<S3Client> s3ClientProvider)
            throws Exception {
        if (properties.isS3()) {
            S3Client client = s3ClientProvider.getIfAvailable();
            String bucket = properties.getS3().getBucket();
            if (client == null || bucket == null || bucket.isBlank()) {
                throw new IllegalStateException("app.storage.type=s3 일 때 S3Client 빈과 app.storage.s3.bucket 이 필요합니다.");
            }
            return new S3BlobStorage(client, bucket);
        }
        Path base = Path.of(properties.getLocal().getBasePath());
        return new LocalBlobStorage(base);
    }
}

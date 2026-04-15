package com.pch.mng.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** {@code local} 또는 {@code s3} */
    private String type = "local";

    private final Local local = new Local();
    private final S3 s3 = new S3();

    @Getter
    @Setter
    public static class Local {
        private String basePath = "./data/attachments";
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket = "";
        private String region = "ap-northeast-2";
        /** MinIO 등 호환 스토리지용 (비우면 AWS 기본 엔드포인트) */
        private String endpoint = "";
        private String accessKey = "";
        private String secretKey = "";
    }

    public boolean isS3() {
        return "s3".equalsIgnoreCase(type);
    }
}

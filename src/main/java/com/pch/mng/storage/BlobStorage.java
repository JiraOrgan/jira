package com.pch.mng.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * 첨부 바이너리 저장 추상화. 구현체는 객체 키(상대 경로 또는 S3 object key)로 식별한다.
 */
public interface BlobStorage {

    /**
     * 스트림을 저장하고, DB에 보관할 동일한 객체 키를 반환한다.
     */
    String put(String objectKey, InputStream data, long contentLength, String contentType) throws IOException;

    InputStream get(String objectKey) throws IOException;

    void delete(String objectKey) throws IOException;
}

package com.pch.mng.integration.github;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesGcmStringEncryptor {

    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;
    private final SecureRandom random = new SecureRandom();

    public AesGcmStringEncryptor(IntegrationCryptoProperties props) {
        if (!StringUtils.hasText(props.getSecret())) {
            this.secretKey = null;
            return;
        }
        try {
            byte[] raw = MessageDigest.getInstance("SHA-256")
                    .digest(props.getSecret().getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(raw, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("integration crypto init failed", e);
        }
    }

    public boolean isConfigured() {
        return secretKey != null;
    }

    public String encrypt(String plainText) {
        assertConfigured();
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherText.length);
            buf.put(iv);
            buf.put(cipherText);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String decrypt(String encoded) {
        assertConfigured();
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            byte[] iv = Arrays.copyOfRange(all, 0, IV_LENGTH);
            byte[] cipherBytes = Arrays.copyOfRange(all, IV_LENGTH, all.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void assertConfigured() {
        if (secretKey == null) {
            throw new BusinessException(ErrorCode.GITHUB_CRYPTO_NOT_CONFIGURED);
        }
    }
}

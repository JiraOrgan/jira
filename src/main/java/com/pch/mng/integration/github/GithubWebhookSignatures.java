package com.pch.mng.integration.github;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

final class GithubWebhookSignatures {

    private GithubWebhookSignatures() {}

    static boolean isValid(String secret, byte[] rawBody, String signatureHeader) {
        if (secret == null
                || rawBody == null
                || signatureHeader == null
                || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        String hex = signatureHeader.substring("sha256=".length()).trim();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody);
            byte[] expected = HexFormat.of().parseHex(hex);
            return MessageDigest.isEqual(digest, expected);
        } catch (Exception e) {
            return false;
        }
    }
}

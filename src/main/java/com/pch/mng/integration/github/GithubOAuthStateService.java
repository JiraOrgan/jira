package com.pch.mng.integration.github;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GithubOAuthStateService {

    private static final long TTL_SECONDS = 600;

    private final GithubOAuthProperties oauthProperties;

    public String createState(long projectId, long userId) {
        if (!StringUtils.hasText(oauthProperties.getStateHmacSecret())) {
            throw new BusinessException(ErrorCode.GITHUB_NOT_CONFIGURED);
        }
        long exp = Instant.now().getEpochSecond() + TTL_SECONDS;
        String payload = projectId + ":" + userId + ":" + exp;
        String sig = hmacHex(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((payload + ":" + sig).getBytes(StandardCharsets.UTF_8));
    }

    public ParsedState verifyAndParse(String state) {
        if (!StringUtils.hasText(state)) {
            throw new BusinessException(ErrorCode.GITHUB_OAUTH_INVALID_STATE);
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            int last = decoded.lastIndexOf(':');
            if (last <= 0) {
                throw new BusinessException(ErrorCode.GITHUB_OAUTH_INVALID_STATE);
            }
            String payload = decoded.substring(0, last);
            String sig = decoded.substring(last + 1);
            if (!MessageDigestSafe.constantTimeEquals(hmacHex(payload), sig)) {
                throw new BusinessException(ErrorCode.GITHUB_OAUTH_INVALID_STATE);
            }
            String[] parts = payload.split(":");
            if (parts.length != 3) {
                throw new BusinessException(ErrorCode.GITHUB_OAUTH_INVALID_STATE);
            }
            long projectId = Long.parseLong(parts[0]);
            long userId = Long.parseLong(parts[1]);
            long exp = Long.parseLong(parts[2]);
            if (Instant.now().getEpochSecond() > exp) {
                throw new BusinessException(ErrorCode.GITHUB_OAUTH_INVALID_STATE);
            }
            return new ParsedState(projectId, userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITHUB_OAUTH_INVALID_STATE);
        }
    }

    private String hmacHex(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    oauthProperties.getStateHmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public record ParsedState(long projectId, long userId) {}

    private static final class MessageDigestSafe {
        static boolean constantTimeEquals(String a, String b) {
            if (a == null || b == null || a.length() != b.length()) {
                return false;
            }
            int r = 0;
            for (int i = 0; i < a.length(); i++) {
                r |= a.charAt(i) ^ b.charAt(i);
            }
            return r == 0;
        }
    }
}

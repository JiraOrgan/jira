package com.pch.mng.notification;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

    /** false면 멘션 알림 전체 비활성(로그·외부 전송 없음). */
    private boolean enabled = true;

    private Slack slack = new Slack();

    private Mail mail = new Mail();

    @Data
    public static class Slack {
        /** Incoming Webhook URL. 비어 있으면 Slack 전송 생략. */
        private String webhookUrl = "";
    }

    @Data
    public static class Mail {
        /** 발신 주소 (JavaMailSender 사용 시 필수). */
        private String from = "noreply@localhost";
    }
}

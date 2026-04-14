package com.pch.mng.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationProperties props;
    private final UserAccountRepository userAccountRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ObjectMapper objectMapper;

    public void notifyCommentMentions(CommentMentionNotificationEvent event) {
        if (!props.isEnabled()) {
            return;
        }
        if (event.mentionedUserIds() == null || event.mentionedUserIds().isEmpty()) {
            return;
        }
        String slackText = formatSlackLine(event);
        log.info(
                "[notify] comment mention issue={} project={} targets={}",
                event.issueKey(),
                event.projectKey(),
                event.mentionedUserIds().size());
        postSlackIfConfigured(slackText);

        String emailSubject =
                String.format("[PCH] %s — 멘션 (%s)", event.projectKey(), event.issueKey());
        for (Long uid : event.mentionedUserIds()) {
            if (uid == null || uid.equals(event.authorUserId())) {
                continue;
            }
            UserAccount target = userAccountRepository.findById(uid).orElse(null);
            if (target == null || !StringUtils.hasText(target.getEmail())) {
                continue;
            }
            String body = formatEmailBody(event, target.getName());
            sendEmailIfConfigured(target.getEmail(), emailSubject, body);
        }
    }

    private String formatSlackLine(CommentMentionNotificationEvent e) {
        String author = e.authorName() != null ? e.authorName() : "Someone";
        String preview = e.bodyPreview() != null ? e.bodyPreview() : "";
        return String.format(
                "*%s* mentioned people in `%s` / `%s`\n```%s```",
                author, e.projectKey(), e.issueKey(), preview.replace("```", "'''"));
    }

    private String formatEmailBody(CommentMentionNotificationEvent e, String recipientName) {
        String name = recipientName != null ? recipientName : "";
        String author = e.authorName() != null ? e.authorName() : "Someone";
        String preview = e.bodyPreview() != null ? e.bodyPreview() : "";
        return String.format(
                "안녕하세요 %s님,%n%n%s 님이 프로젝트 %s의 이슈 %s 댓글에서 멘션했습니다.%n%n---%n%s%n",
                name, author, e.projectKey(), e.issueKey(), preview);
    }

    private void postSlackIfConfigured(String text) {
        String url = props.getSlack().getWebhookUrl();
        if (!StringUtils.hasText(url)) {
            return;
        }
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("text", text);
            String json = objectMapper.writeValueAsString(payload);
            RestClient.create()
                    .post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();
        } catch (JsonProcessingException ex) {
            log.warn("slack payload build failed: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("slack webhook failed: {}", ex.getMessage());
        }
    }

    private void sendEmailIfConfigured(String to, String subject, String text) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.debug("JavaMailSender 없음 — 이메일 생략 (to={})", to);
            return;
        }
        String from = props.getMail().getFrom();
        if (!StringUtils.hasText(from)) {
            log.warn("app.notification.mail.from 미설정 — 이메일 생략");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            sender.send(msg);
        } catch (Exception ex) {
            log.warn("mail send failed to {}: {}", to, ex.getMessage());
        }
    }
}

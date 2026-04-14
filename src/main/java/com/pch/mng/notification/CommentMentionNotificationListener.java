package com.pch.mng.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CommentMentionNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(CommentMentionNotificationListener.class);

    private final NotificationService notificationService;

    public CommentMentionNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentMentions(CommentMentionNotificationEvent event) {
        try {
            notificationService.notifyCommentMentions(event);
        } catch (Exception ex) {
            log.warn("comment mention notification failed: {}", ex.getMessage());
        }
    }
}

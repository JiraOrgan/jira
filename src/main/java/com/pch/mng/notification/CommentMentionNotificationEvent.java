package com.pch.mng.notification;

import java.util.Set;

/**
 * 댓글 저장/수정 커밋 이후 비동기로 전달. {@link CommentMentionNotificationListener}에서 소비.
 */
public record CommentMentionNotificationEvent(
        String issueKey,
        String projectKey,
        Long authorUserId,
        String authorName,
        String bodyPreview,
        Set<Long> mentionedUserIds) {}

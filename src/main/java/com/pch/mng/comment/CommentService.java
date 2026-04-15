package com.pch.mng.comment;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.notification.CommentMentionNotificationEvent;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final CommentMentionResolver commentMentionResolver;
    private final IssueRepository issueRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<CommentResponse.DetailDTO> findByIssue(Long issueId) {
        List<Comment> comments = commentRepository.findByIssueIdWithAuthor(issueId);
        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        List<CommentMention> mentionRows =
                commentMentionRepository.findByComment_IdInWithUser(commentIds);
        Map<Long, List<CommentMention>> byCommentId =
                mentionRows.stream()
                        .collect(Collectors.groupingBy(m -> m.getComment().getId()));
        return comments.stream()
                .map(
                        c ->
                                CommentResponse.DetailDTO.of(
                                        c, toMentionDtos(byCommentId.get(c.getId()))))
                .toList();
    }

    private static List<CommentResponse.MentionDTO> toMentionDtos(List<CommentMention> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<CommentResponse.MentionDTO> list = new ArrayList<>(rows.size());
        for (CommentMention m : rows) {
            list.add(CommentResponse.MentionDTO.of(m.getUser()));
        }
        return list;
    }

    @Transactional
    public CommentResponse.DetailDTO save(CommentRequest.SaveDTO reqDTO, Long authorId) {
        Issue issue =
                issueRepository
                        .findById(reqDTO.getIssueId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount author =
                userAccountRepository
                        .findById(authorId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Comment comment =
                Comment.builder()
                        .issue(issue)
                        .author(author)
                        .body(reqDTO.getBody())
                        .build();
        commentRepository.save(comment);
        Set<Long> mentioned =
                replaceMentions(comment.getId(), issue.getProject().getId(), comment.getBody());
        publishMentionEventIfNeeded(issue, author, comment.getBody(), mentioned);
        return CommentResponse.DetailDTO.of(
                comment, mentionDtosForComment(comment.getId()));
    }

    @Transactional
    public CommentResponse.DetailDTO update(Long id, CommentRequest.UpdateDTO reqDTO) {
        Comment comment =
                commentRepository
                        .findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        comment.setBody(reqDTO.getBody());
        commentRepository.save(comment);
        Long projectId =
                commentRepository
                        .findIssueProjectIdByCommentId(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Set<Long> mentioned = replaceMentions(id, projectId, comment.getBody());
        publishMentionEventIfNeeded(comment.getIssue(), comment.getAuthor(), comment.getBody(), mentioned);
        return CommentResponse.DetailDTO.of(comment, mentionDtosForComment(id));
    }

    @Transactional
    public void delete(Long id) {
        Comment comment =
                commentRepository
                        .findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        commentMentionRepository.deleteByComment_Id(id);
        commentRepository.delete(comment);
    }

    private Set<Long> replaceMentions(Long commentId, Long projectId, String body) {
        commentMentionRepository.deleteByComment_Id(commentId);
        Set<Long> userIds = commentMentionResolver.resolveMentionedUserIds(projectId, body);
        if (userIds.isEmpty()) {
            return Set.of();
        }
        Comment ref = commentRepository.getReferenceById(commentId);
        List<CommentMention> rows = new ArrayList<>();
        for (Long uid : userIds) {
            rows.add(
                    CommentMention.builder()
                            .comment(ref)
                            .user(userAccountRepository.getReferenceById(uid))
                            .build());
        }
        commentMentionRepository.saveAll(rows);
        return Set.copyOf(userIds);
    }

    private void publishMentionEventIfNeeded(
            Issue issue, UserAccount author, String body, Set<Long> mentionedUserIds) {
        if (mentionedUserIds.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(
                new CommentMentionNotificationEvent(
                        issue.getIssueKey(),
                        issue.getProject().getKey(),
                        author.getId(),
                        author.getName(),
                        CommentBodyPreview.of(body),
                        Set.copyOf(mentionedUserIds)));
    }

    private List<CommentResponse.MentionDTO> mentionDtosForComment(Long commentId) {
        List<CommentMention> rows =
                commentMentionRepository.findByComment_IdInWithUser(List.of(commentId));
        return toMentionDtos(rows);
    }
}

package com.pch.mng.comment;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final UserAccountRepository userAccountRepository;

    public List<CommentResponse.DetailDTO> findByIssue(Long issueId) {
        return commentRepository.findByIssueIdWithAuthor(issueId).stream()
                .map(CommentResponse.DetailDTO::of)
                .toList();
    }

    @Transactional
    public CommentResponse.DetailDTO save(CommentRequest.SaveDTO reqDTO, Long authorId) {
        Issue issue = issueRepository.findById(reqDTO.getIssueId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount author = userAccountRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Comment comment = Comment.builder()
                .issue(issue)
                .author(author)
                .body(reqDTO.getBody())
                .build();
        commentRepository.save(comment);
        return CommentResponse.DetailDTO.of(comment);
    }

    @Transactional
    public CommentResponse.DetailDTO update(Long id, CommentRequest.UpdateDTO reqDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        comment.setBody(reqDTO.getBody());
        return CommentResponse.DetailDTO.of(comment);
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        commentRepository.delete(comment);
    }
}

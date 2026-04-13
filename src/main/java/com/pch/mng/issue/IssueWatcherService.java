package com.pch.mng.issue;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class IssueWatcherService {

    private final IssueRepository issueRepository;
    private final IssueWatcherRepository issueWatcherRepository;
    private final UserAccountRepository userAccountRepository;

    public IssueWatcherResponse.ListDTO listForIssue(String issueKey, Long currentUserId) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        List<IssueWatcher> rows = issueWatcherRepository.findByIssueIdWithUserOrderByName(issue.getId());
        IssueWatcherResponse.ListDTO dto = new IssueWatcherResponse.ListDTO();
        dto.setWatchers(rows.stream().map(IssueWatcherResponse.UserDTO::of).toList());
        dto.setSelfWatching(
                currentUserId != null && issueWatcherRepository.existsByIssueIdAndUserId(issue.getId(), currentUserId));
        return dto;
    }

    @Transactional
    public void watch(String issueKey, Long userId) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (issueWatcherRepository.existsByIssueIdAndUserId(issue.getId(), userId)) {
            return;
        }
        IssueWatcher row = new IssueWatcher();
        row.setIssue(issue);
        row.setUser(user);
        issueWatcherRepository.save(row);
    }

    @Transactional
    public void unwatch(String issueKey, Long userId) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueWatcherRepository.deleteByIssueIdAndUserId(issue.getId(), userId);
    }
}

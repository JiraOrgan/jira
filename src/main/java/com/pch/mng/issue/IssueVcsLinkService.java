package com.pch.mng.issue;

import com.pch.mng.global.enums.VcsLinkKind;
import com.pch.mng.global.enums.VcsProvider;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class IssueVcsLinkService {

    private final IssueRepository issueRepository;
    private final IssueVcsLinkRepository issueVcsLinkRepository;
    private final UserAccountRepository userAccountRepository;

    public List<IssueVcsLinkResponse.DetailDTO> findByIssueKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return issueVcsLinkRepository.findByIssue_IdOrderByIdDesc(issue.getId()).stream()
                .map(IssueVcsLinkResponse.DetailDTO::of)
                .toList();
    }

    @Transactional
    public IssueVcsLinkResponse.DetailDTO add(String issueKey, Long actorUserId, IssueVcsLinkRequest.SaveDTO dto) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (issue.isArchived()) {
            throw new BusinessException(ErrorCode.ISSUE_ARCHIVED);
        }
        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String url = dto.getUrl().trim();
        assertValidHttpUrl(url);
        if (issueVcsLinkRepository.existsByIssue_IdAndUrl(issue.getId(), url)) {
            throw new BusinessException(ErrorCode.VCS_LINK_DUPLICATE);
        }

        String title = StringUtils.hasText(dto.getTitle()) ? dto.getTitle().trim() : null;

        IssueVcsLink link =
                IssueVcsLink.builder()
                        .issue(issue)
                        .provider(dto.getProvider())
                        .linkKind(dto.getLinkKind())
                        .url(url)
                        .title(title)
                        .createdBy(actor)
                        .build();
        issueVcsLinkRepository.save(link);
        return IssueVcsLinkResponse.DetailDTO.of(link);
    }

    @Transactional
    public void delete(String issueKey, Long linkId) {
        Issue issue = issueRepository.findByIssueKeyWithProject(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        IssueVcsLink link = issueVcsLinkRepository
                .findByIdAndIssue_Id(linkId, issue.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueVcsLinkRepository.delete(link);
    }

    /**
     * GitHub 웹훅 등 자동화 경로에서 VCS 링크를 추가한다. URL이 이미 있으면 무시한다.
     */
    @Transactional
    public void ensureGithubLinkFromAutomation(Issue issue, VcsLinkKind linkKind, String url, String title) {
        if (issue.isArchived()) {
            return;
        }
        assertValidHttpUrl(url);
        if (issueVcsLinkRepository.existsByIssue_IdAndUrl(issue.getId(), url)) {
            return;
        }
        String t = title != null && !title.isBlank() ? truncate(title.trim(), 500) : null;
        IssueVcsLink link = IssueVcsLink.builder()
                .issue(issue)
                .provider(VcsProvider.GITHUB)
                .linkKind(linkKind)
                .url(url)
                .title(t)
                .createdBy(issue.getReporter())
                .build();
        issueVcsLinkRepository.save(link);
    }

    private static void assertValidHttpUrl(String url) {
        try {
            URI u = URI.create(url);
            String scheme = u.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http"))) {
                throw new BusinessException(ErrorCode.VCS_LINK_INVALID_URL);
            }
            if (u.getHost() == null || u.getHost().isBlank()) {
                throw new BusinessException(ErrorCode.VCS_LINK_INVALID_URL);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VCS_LINK_INVALID_URL);
        }
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}

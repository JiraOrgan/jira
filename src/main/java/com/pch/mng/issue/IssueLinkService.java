package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueLinkType;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class IssueLinkService {

    private final IssueRepository issueRepository;
    private final IssueLinkRepository issueLinkRepository;

    public List<IssueLinkResponse.DetailDTO> findByIssueKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return issueLinkRepository.findByIssueIdWithIssues(issue.getId()).stream()
                .map(IssueLinkResponse.DetailDTO::of)
                .toList();
    }

    @Transactional
    public IssueLinkResponse.DetailDTO create(String sourceIssueKey, IssueLinkRequest.SaveDTO reqDTO) {
        Issue source = issueRepository.findByIssueKeyWithProject(sourceIssueKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Issue target = issueRepository.findByIssueKeyWithProject(reqDTO.getTargetIssueKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        if (source.getId().equals(target.getId())) {
            throw new BusinessException(ErrorCode.ISSUE_LINK_SELF);
        }
        if (!source.getProject().getId().equals(target.getProject().getId())) {
            throw new BusinessException(ErrorCode.ISSUE_LINK_PROJECT_MISMATCH);
        }
        if (issueLinkRepository.existsBySourceIssue_IdAndTargetIssue_IdAndLinkType(
                source.getId(), target.getId(), reqDTO.getLinkType())) {
            throw new BusinessException(ErrorCode.ISSUE_LINK_DUPLICATE);
        }

        IssueLink link = IssueLink.builder()
                .sourceIssue(source)
                .targetIssue(target)
                .linkType(reqDTO.getLinkType())
                .build();
        issueLinkRepository.save(link);
        return IssueLinkResponse.DetailDTO.of(link);
    }

    @Transactional
    public IssueLinkResponse.DetailDTO update(Long linkId, IssueLinkRequest.UpdateDTO reqDTO) {
        IssueLink link = issueLinkRepository.findByIdWithIssues(linkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Long sid = link.getSourceIssue().getId();
        Long tid = link.getTargetIssue().getId();
        IssueLinkType newType = reqDTO.getLinkType();
        if (link.getLinkType() != newType
                && issueLinkRepository.existsBySourceIssue_IdAndTargetIssue_IdAndLinkType(sid, tid, newType)) {
            throw new BusinessException(ErrorCode.ISSUE_LINK_DUPLICATE);
        }
        link.setLinkType(newType);
        return IssueLinkResponse.DetailDTO.of(link);
    }

    @Transactional
    public void delete(Long linkId) {
        IssueLink link = issueLinkRepository.findByIdWithIssues(linkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        issueLinkRepository.delete(link);
    }
}

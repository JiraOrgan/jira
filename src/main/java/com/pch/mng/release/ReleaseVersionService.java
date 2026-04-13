package com.pch.mng.release;

import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.enums.VersionStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueFixVersion;
import com.pch.mng.issue.IssueFixVersionRepository;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReleaseVersionService {

    private final ReleaseVersionRepository releaseVersionRepository;
    private final ProjectRepository projectRepository;
    private final IssueFixVersionRepository issueFixVersionRepository;

    public List<ReleaseVersionResponse.MinDTO> findByProject(Long projectId) {
        return releaseVersionRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(ReleaseVersionResponse.MinDTO::of)
                .toList();
    }

    public ReleaseVersionResponse.DetailDTO findById(Long id) {
        ReleaseVersion version = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return ReleaseVersionResponse.DetailDTO.of(version);
    }

    @Transactional
    public ReleaseVersionResponse.DetailDTO save(ReleaseVersionRequest.SaveDTO reqDTO) {
        Project project = projectRepository.findById(reqDTO.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        ReleaseVersion version = ReleaseVersion.builder()
                .project(project)
                .name(reqDTO.getName())
                .description(reqDTO.getDescription())
                .releaseDate(reqDTO.getReleaseDate())
                .status(VersionStatus.UNRELEASED)
                .build();
        releaseVersionRepository.save(version);
        return ReleaseVersionResponse.DetailDTO.of(version);
    }

    @Transactional
    public ReleaseVersionResponse.DetailDTO release(Long id) {
        ReleaseVersion version = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        version.setStatus(VersionStatus.RELEASED);
        return ReleaseVersionResponse.DetailDTO.of(version);
    }

    @Transactional
    public void delete(Long id) {
        ReleaseVersion version = releaseVersionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        releaseVersionRepository.delete(version);
    }

    /**
     * Fix 버전에 연결된 이슈를 집계해 마크다운 형태의 릴리즈 노트 초안을 만든다 (FR-020).
     */
    public ReleaseNotesResponse.DTO generateReleaseNotes(Long versionId) {
        ReleaseVersion version = releaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        List<IssueFixVersion> links = issueFixVersionRepository.findByVersionIdWithIssuesOrderByIssueKey(versionId);
        List<ReleaseNotesResponse.IssueLineDTO> lines = new ArrayList<>();
        for (IssueFixVersion link : links) {
            Issue issue = link.getIssue();
            ReleaseNotesResponse.IssueLineDTO row = new ReleaseNotesResponse.IssueLineDTO();
            row.setIssueKey(issue.getIssueKey());
            row.setSummary(issue.getSummary());
            row.setIssueType(issue.getIssueType());
            row.setStatus(issue.getStatus());
            lines.add(row);
        }
        ReleaseNotesResponse.DTO dto = new ReleaseNotesResponse.DTO();
        dto.setVersionId(version.getId());
        dto.setVersionName(version.getName());
        dto.setIssueCount(lines.size());
        dto.setIssues(lines);
        dto.setMarkdown(buildMarkdown(version.getName(), lines));
        return dto;
    }

    private static String buildMarkdown(String versionName, List<ReleaseNotesResponse.IssueLineDTO> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 릴리즈 노트 — ").append(versionName).append("\n\n");
        sb.append("Fix 버전에 연결된 이슈: **").append(lines.size()).append("**건\n\n");
        if (lines.isEmpty()) {
            sb.append("_연결된 이슈가 없습니다._\n");
            return sb.toString();
        }
        Map<IssueType, List<ReleaseNotesResponse.IssueLineDTO>> byType = lines.stream()
                .collect(Collectors.groupingBy(ReleaseNotesResponse.IssueLineDTO::getIssueType, () -> new EnumMap<>(IssueType.class), Collectors.toList()));
        for (IssueType type : IssueType.values()) {
            List<ReleaseNotesResponse.IssueLineDTO> group = byType.get(type);
            if (group == null || group.isEmpty()) {
                continue;
            }
            group.sort((a, b) -> a.getIssueKey().compareToIgnoreCase(b.getIssueKey()));
            sb.append("## ").append(typeHeadingKo(type)).append("\n\n");
            for (ReleaseNotesResponse.IssueLineDTO row : group) {
                sb.append("- **").append(row.getIssueKey()).append("** ")
                        .append(escapeMarkdownInline(row.getSummary()))
                        .append(" — `").append(row.getStatus()).append("`\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String typeHeadingKo(IssueType type) {
        return switch (type) {
            case EPIC -> "Epic";
            case STORY -> "스토리";
            case TASK -> "작업";
            case BUG -> "버그";
            case SUBTASK -> "하위 작업";
        };
    }

    private static String escapeMarkdownInline(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("\r\n", " ")
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
    }
}

package com.jira.mng.release;

import com.jira.mng.global.enums.VersionStatus;
import com.jira.mng.global.exception.BusinessException;
import com.jira.mng.global.exception.ErrorCode;
import com.jira.mng.project.Project;
import com.jira.mng.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReleaseVersionService {

    private final ReleaseVersionRepository releaseVersionRepository;
    private final ProjectRepository projectRepository;

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
}

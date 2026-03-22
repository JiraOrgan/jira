package com.pch.mng.sprint;

import com.pch.mng.global.enums.SprintStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    public List<SprintResponse.MinDTO> findByProject(Long projectId) {
        return sprintRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(SprintResponse.MinDTO::of)
                .toList();
    }

    public SprintResponse.DetailDTO findById(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return SprintResponse.DetailDTO.of(sprint);
    }

    @Transactional
    public SprintResponse.DetailDTO save(SprintRequest.SaveDTO reqDTO) {
        Project project = projectRepository.findById(reqDTO.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Sprint sprint = Sprint.builder()
                .project(project)
                .name(reqDTO.getName())
                .status(SprintStatus.PLANNING)
                .startDate(reqDTO.getStartDate())
                .endDate(reqDTO.getEndDate())
                .goalPoints(reqDTO.getGoalPoints())
                .build();
        sprintRepository.save(sprint);
        return SprintResponse.DetailDTO.of(sprint);
    }

    @Transactional
    public SprintResponse.DetailDTO start(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        sprint.setStatus(SprintStatus.ACTIVE);
        return SprintResponse.DetailDTO.of(sprint);
    }

    @Transactional
    public SprintResponse.DetailDTO complete(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        sprint.setStatus(SprintStatus.COMPLETED);
        return SprintResponse.DetailDTO.of(sprint);
    }

    @Transactional
    public void delete(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        sprintRepository.delete(sprint);
    }
}

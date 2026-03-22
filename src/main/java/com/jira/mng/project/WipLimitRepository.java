package com.jira.mng.project;

import com.jira.mng.global.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WipLimitRepository extends JpaRepository<WipLimit, Long> {
    List<WipLimit> findByProjectId(Long projectId);
    Optional<WipLimit> findByProjectIdAndStatus(Long projectId, IssueStatus status);
}

package com.pch.mng.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    List<WorkflowTransition> findByIssueIdOrderByTransitionedAtDesc(Long issueId);
}

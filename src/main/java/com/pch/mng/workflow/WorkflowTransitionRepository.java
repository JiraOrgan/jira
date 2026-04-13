package com.pch.mng.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    List<WorkflowTransition> findByIssueIdOrderByTransitionedAtDesc(Long issueId);

    @Query("SELECT wt FROM WorkflowTransition wt JOIN FETCH wt.changedBy WHERE wt.issue.id = :issueId ORDER BY wt.transitionedAt DESC")
    List<WorkflowTransition> findByIssueIdWithActorOrderByTransitionedAtDesc(@Param("issueId") Long issueId);

    @Query("SELECT wt FROM WorkflowTransition wt WHERE wt.issue.id IN :issueIds ORDER BY wt.issue.id ASC, wt.transitionedAt ASC")
    List<WorkflowTransition> findByIssueIdInOrderByIssueAndTimeAsc(@Param("issueIds") Collection<Long> issueIds);
}

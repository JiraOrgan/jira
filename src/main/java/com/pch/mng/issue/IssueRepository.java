package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    Optional<Issue> findByIssueKey(String issueKey);
    List<Issue> findByProjectIdAndSprintIdOrderByCreatedAtDesc(Long projectId, Long sprintId);
    List<Issue> findByProjectIdAndStatusOrderByCreatedAtDesc(Long projectId, IssueStatus status);
    List<Issue> findByProjectIdAndSprintIsNullOrderByCreatedAtDesc(Long projectId);
    List<Issue> findByAssigneeIdOrderByUpdatedAtDesc(Long assigneeId);
    Page<Issue> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
    long countByProjectIdAndStatus(Long projectId, IssueStatus status);

    long countBySprint_Id(Long sprintId);

    @Query("SELECT i FROM Issue i JOIN FETCH i.project LEFT JOIN FETCH i.assignee LEFT JOIN FETCH i.reporter WHERE i.issueKey = :key")
    Optional<Issue> findByIssueKeyWithDetails(@Param("key") String issueKey);

    @Query("SELECT i FROM Issue i JOIN FETCH i.project JOIN FETCH i.reporter LEFT JOIN FETCH i.assignee WHERE i.issueKey = :key")
    Optional<Issue> findByIssueKeyWithProject(@Param("key") String issueKey);

    @Query("SELECT i FROM Issue i JOIN FETCH i.project WHERE i.id = :id")
    Optional<Issue> findByIdWithProject(@Param("id") Long id);
}

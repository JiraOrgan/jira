package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    Optional<Issue> findByIssueKey(String issueKey);

    List<Issue> findByProjectIdAndSprintIdAndArchivedFalseOrderByCreatedAtDesc(
            Long projectId, Long sprintId);

    List<Issue> findByProjectIdAndStatusAndArchivedFalseOrderByCreatedAtDesc(
            Long projectId, IssueStatus status);

    List<Issue> findByProjectIdAndSprintIsNullAndArchivedFalseOrderByBacklogRankAscIdAsc(
            Long projectId);

    @Query(
            "SELECT COALESCE(MAX(i.backlogRank), 0) FROM Issue i WHERE i.project.id = :pid AND i.sprint IS NULL AND i.archived = false")
    long maxBacklogRankForProjectBacklog(@Param("pid") Long projectId);

    List<Issue> findByAssigneeIdAndArchivedFalseOrderByUpdatedAtDesc(Long assigneeId);

    Page<Issue> findByProjectIdAndArchivedFalseOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    long countByProjectIdAndStatusAndArchivedFalse(Long projectId, IssueStatus status);

    long countBySprint_IdAndArchivedFalse(Long sprintId);

    @Query(
            "SELECT i FROM Issue i JOIN FETCH i.project LEFT JOIN FETCH i.assignee LEFT JOIN FETCH i.reporter WHERE i.issueKey = :key")
    Optional<Issue> findByIssueKeyWithDetails(@Param("key") String issueKey);

    @Query(
            "SELECT i FROM Issue i JOIN FETCH i.project JOIN FETCH i.reporter LEFT JOIN FETCH i.assignee LEFT JOIN FETCH i.sprint WHERE i.issueKey = :key")
    Optional<Issue> findByIssueKeyWithProject(@Param("key") String issueKey);

    @Query(
            "SELECT i FROM Issue i JOIN FETCH i.project JOIN FETCH i.reporter LEFT JOIN FETCH i.assignee LEFT JOIN FETCH i.sprint WHERE i.id = :id")
    Optional<Issue> findByIdWithProject(@Param("id") Long id);

    @Query(
            "SELECT DISTINCT i FROM Issue i JOIN FETCH i.reporter LEFT JOIN FETCH i.assignee WHERE i.sprint.id = :sid AND i.archived = false")
    List<Issue> findForSprintBoard(@Param("sid") Long sprintId);

    List<Issue> findByProjectIdAndIssueTypeAndArchivedFalseOrderByEpicStartDateAscIdAsc(
            Long projectId, IssueType issueType);

    @Query(
            "SELECT COALESCE(SUM(i.storyPoints), 0) FROM Issue i WHERE i.sprint.id = :sprintId AND i.status = :status AND i.archived = false")
    Long sumStoryPointsBySprintIdAndStatus(
            @Param("sprintId") Long sprintId, @Param("status") IssueStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "UPDATE Issue i SET i.archived = true WHERE i.project.id = :pid AND i.status = :done AND i.archived = false AND i.updatedAt < :cutoff")
    int archiveDoneIssuesOlderThan(
            @Param("pid") Long projectId,
            @Param("done") IssueStatus done,
            @Param("cutoff") LocalDateTime cutoff);
}

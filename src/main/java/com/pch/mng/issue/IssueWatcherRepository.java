package com.pch.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueWatcherRepository extends JpaRepository<IssueWatcher, Long> {
    List<IssueWatcher> findByIssueId(Long issueId);
    boolean existsByIssueIdAndUserId(Long issueId, Long userId);
    void deleteByIssueIdAndUserId(Long issueId, Long userId);

    @Query("SELECT w FROM IssueWatcher w JOIN FETCH w.user u WHERE w.issue.id = :issueId ORDER BY u.name ASC, u.id ASC")
    List<IssueWatcher> findByIssueIdWithUserOrderByName(@Param("issueId") Long issueId);
}

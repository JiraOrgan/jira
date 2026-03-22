package com.jira.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface IssueLinkRepository extends JpaRepository<IssueLink, Long> {
    @Query("SELECT il FROM IssueLink il WHERE il.sourceIssue.id = :issueId OR il.targetIssue.id = :issueId")
    List<IssueLink> findByIssueId(@Param("issueId") Long issueId);
}

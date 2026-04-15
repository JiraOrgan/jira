package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueLinkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueLinkRepository extends JpaRepository<IssueLink, Long> {

    boolean existsBySourceIssue_IdAndTargetIssue_IdAndLinkType(Long sourceIssueId, Long targetIssueId,
            IssueLinkType linkType);

    @Query("""
            SELECT il FROM IssueLink il
            JOIN FETCH il.sourceIssue s JOIN FETCH s.project
            JOIN FETCH il.targetIssue t JOIN FETCH t.project
            WHERE il.id = :id
            """)
    Optional<IssueLink> findByIdWithIssues(@Param("id") Long id);

    @Query("""
            SELECT il FROM IssueLink il
            JOIN FETCH il.sourceIssue s JOIN FETCH s.project
            JOIN FETCH il.targetIssue t JOIN FETCH t.project
            WHERE s.id = :issueId OR t.id = :issueId
            """)
    List<IssueLink> findByIssueIdWithIssues(@Param("issueId") Long issueId);
}

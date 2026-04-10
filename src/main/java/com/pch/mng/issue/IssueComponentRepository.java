package com.pch.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueComponentRepository extends JpaRepository<IssueComponent, Long> {

    List<IssueComponent> findByIssueId(Long issueId);

    boolean existsByIssue_IdAndComponent_Id(Long issueId, Long componentId);

    void deleteByIssue_IdAndComponent_Id(Long issueId, Long componentId);

    @Query("""
            SELECT ic FROM IssueComponent ic
            JOIN FETCH ic.component c JOIN FETCH c.project
            WHERE ic.issue.id = :issueId
            ORDER BY c.name
            """)
    List<IssueComponent> findByIssueIdWithComponent(@Param("issueId") Long issueId);
}

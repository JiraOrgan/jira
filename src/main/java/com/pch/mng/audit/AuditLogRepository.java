package com.pch.mng.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @EntityGraph(attributePaths = {"issue", "changedBy"})
    Page<AuditLog> findByIssueIdOrderByChangedAtDesc(Long issueId, Pageable pageable);

    @EntityGraph(attributePaths = {"issue", "changedBy"})
    Page<AuditLog> findByIssue_Project_IdOrderByChangedAtDesc(Long projectId, Pageable pageable);

    void deleteByIssue_Id(Long issueId);
}

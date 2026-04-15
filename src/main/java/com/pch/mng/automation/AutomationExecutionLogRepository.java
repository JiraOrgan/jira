package com.pch.mng.automation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomationExecutionLogRepository extends JpaRepository<AutomationExecutionLog, Long> {

    @EntityGraph(attributePaths = {"rule", "issue"})
    Page<AutomationExecutionLog> findByRule_Project_IdOrderByIdDesc(Long projectId, Pageable pageable);

    void deleteByRule_Id(Long ruleId);
}

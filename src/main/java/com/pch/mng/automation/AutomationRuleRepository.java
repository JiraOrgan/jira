package com.pch.mng.automation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    @Query(
            "select r from AutomationRule r where r.project.id = :projectId and r.enabled = true order by"
                    + " r.sortOrder asc, r.id asc")
    List<AutomationRule> findEnabledByProjectIdOrderBySortOrderAscIdAsc(@Param("projectId") Long projectId);

    List<AutomationRule> findByProject_IdOrderBySortOrderAscIdAsc(Long projectId);

    Optional<AutomationRule> findByIdAndProject_Id(Long id, Long projectId);
}

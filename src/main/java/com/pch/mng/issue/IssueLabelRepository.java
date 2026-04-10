package com.pch.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {

    List<IssueLabel> findByIssueId(Long issueId);

    boolean existsByIssue_IdAndLabel_Id(Long issueId, Long labelId);

    void deleteByIssueIdAndLabelId(Long issueId, Long labelId);

    @Query("SELECT il FROM IssueLabel il JOIN FETCH il.label WHERE il.issue.id = :issueId ORDER BY il.label.name")
    List<IssueLabel> findByIssueIdWithLabel(@Param("issueId") Long issueId);
}

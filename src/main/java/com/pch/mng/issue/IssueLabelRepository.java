package com.pch.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {
    List<IssueLabel> findByIssueId(Long issueId);
    void deleteByIssueIdAndLabelId(Long issueId, Long labelId);
}

package com.pch.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueComponentRepository extends JpaRepository<IssueComponent, Long> {
    List<IssueComponent> findByIssueId(Long issueId);
}

package com.jira.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueFixVersionRepository extends JpaRepository<IssueFixVersion, Long> {
    List<IssueFixVersion> findByIssueId(Long issueId);
    List<IssueFixVersion> findByVersionId(Long versionId);
}

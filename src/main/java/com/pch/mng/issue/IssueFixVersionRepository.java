package com.pch.mng.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueFixVersionRepository extends JpaRepository<IssueFixVersion, Long> {
    List<IssueFixVersion> findByIssueId(Long issueId);
    List<IssueFixVersion> findByVersionId(Long versionId);

    @Query("SELECT ifv FROM IssueFixVersion ifv JOIN FETCH ifv.issue i WHERE ifv.version.id = :versionId ORDER BY i.issueKey ASC")
    List<IssueFixVersion> findByVersionIdWithIssuesOrderByIssueKey(@Param("versionId") Long versionId);
}

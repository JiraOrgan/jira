package com.pch.mng.issue;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueVcsLinkRepository extends JpaRepository<IssueVcsLink, Long> {

    @EntityGraph(attributePaths = "createdBy")
    List<IssueVcsLink> findByIssue_IdOrderByIdDesc(Long issueId);

    void deleteByIssue_Id(Long issueId);

    boolean existsByIssue_IdAndUrl(Long issueId, String url);

    Optional<IssueVcsLink> findByIdAndIssue_Id(Long id, Long issueId);
}

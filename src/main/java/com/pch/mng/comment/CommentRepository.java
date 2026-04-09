package com.pch.mng.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.issue.id = :issueId ORDER BY c.createdAt ASC")
    List<Comment> findByIssueIdWithAuthor(@Param("issueId") Long issueId);

    @Query("SELECT c.issue.project.id FROM Comment c WHERE c.id = :id")
    Optional<Long> findIssueProjectIdByCommentId(@Param("id") Long id);

    boolean existsByIdAndAuthor_Id(Long id, Long authorId);
}

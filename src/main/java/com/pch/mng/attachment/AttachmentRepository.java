package com.pch.mng.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByIssueIdOrderByCreatedAtDesc(Long issueId);

    @Query("SELECT a FROM Attachment a JOIN FETCH a.uploader WHERE a.issue.id = :issueId ORDER BY a.createdAt DESC")
    List<Attachment> findByIssueIdWithUploaderOrderByCreatedAtDesc(@Param("issueId") Long issueId);

    @Query("SELECT i.issueKey FROM Attachment a JOIN a.issue i WHERE a.id = :id")
    Optional<String> findIssueKeyByAttachmentId(@Param("id") Long id);

    @Query("SELECT a FROM Attachment a JOIN FETCH a.issue i JOIN FETCH a.uploader WHERE a.id = :id")
    Optional<Attachment> findByIdWithIssueAndUploader(@Param("id") Long id);
}

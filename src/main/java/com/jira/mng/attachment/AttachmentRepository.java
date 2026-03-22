package com.jira.mng.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByIssueIdOrderByCreatedAtDesc(Long issueId);
}

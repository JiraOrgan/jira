package com.jira.mng.attachment;

import lombok.Data;
import java.time.LocalDateTime;

public class AttachmentResponse {

    @Data
    public static class DetailDTO {
        private Long id;
        private Long issueId;
        private String uploaderName;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String mimeType;
        private LocalDateTime createdAt;

        private DetailDTO() {}

        public static DetailDTO of(Attachment attachment) {
            DetailDTO dto = new DetailDTO();
            dto.id = attachment.getId();
            dto.fileName = attachment.getFileName();
            dto.filePath = attachment.getFilePath();
            dto.fileSize = attachment.getFileSize();
            dto.mimeType = attachment.getMimeType();
            dto.createdAt = attachment.getCreatedAt();
            if (attachment.getIssue() != null) {
                dto.issueId = attachment.getIssue().getId();
            }
            if (attachment.getUploader() != null) {
                dto.uploaderName = attachment.getUploader().getName();
            }
            return dto;
        }
    }
}

package com.pch.mng.release;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.IssueType;
import lombok.Data;

import java.util.List;

public class ReleaseNotesResponse {

    @Data
    public static class IssueLineDTO {
        private String issueKey;
        private String summary;
        private IssueType issueType;
        private IssueStatus status;
    }

    @Data
    public static class DTO {
        private Long versionId;
        private String versionName;
        private int issueCount;
        private String markdown;
        private List<IssueLineDTO> issues;
    }
}

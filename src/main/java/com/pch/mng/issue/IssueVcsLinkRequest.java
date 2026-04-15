package com.pch.mng.issue;

import com.pch.mng.global.enums.VcsLinkKind;
import com.pch.mng.global.enums.VcsProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class IssueVcsLinkRequest {

    @Data
    public static class SaveDTO {
        @NotNull
        private VcsProvider provider;

        @NotNull
        private VcsLinkKind linkKind;

        @NotBlank
        @Size(max = 2000)
        private String url;

        @Size(max = 500)
        private String title;
    }
}

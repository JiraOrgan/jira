package com.pch.mng.issue;

import com.pch.mng.global.enums.VcsLinkKind;
import com.pch.mng.global.enums.VcsProvider;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

public class IssueVcsLinkResponse {

    @Value
    @Builder
    public static class DetailDTO {
        Long id;
        VcsProvider provider;
        VcsLinkKind linkKind;
        String url;
        String title;
        Long createdById;
        String createdByName;
        LocalDateTime createdAt;

        public static DetailDTO of(IssueVcsLink link) {
            return DetailDTO.builder()
                    .id(link.getId())
                    .provider(link.getProvider())
                    .linkKind(link.getLinkKind())
                    .url(link.getUrl())
                    .title(link.getTitle())
                    .createdById(link.getCreatedBy().getId())
                    .createdByName(link.getCreatedBy().getName())
                    .createdAt(link.getCreatedAt())
                    .build();
        }
    }
}

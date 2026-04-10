package com.pch.mng.jql;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SavedJqlFilterResponse {

    private Long id;
    private String name;
    private String jql;
    private LocalDateTime createdAt;

    public static SavedJqlFilterResponse of(SavedJqlFilter entity) {
        return SavedJqlFilterResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .jql(entity.getJql())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

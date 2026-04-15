package com.pch.mng.jql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JqlSearchRequest {

    @NotBlank
    @Size(max = 4000)
    private String jql;

    /** Jira 스타일 오프셋 (기본 0) */
    private Integer startAt;

    /** 페이지 크기 (서버 상한 적용) */
    private Integer maxResults;
}

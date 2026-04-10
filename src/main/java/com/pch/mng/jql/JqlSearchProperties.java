package com.pch.mng.jql;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jql")
public class JqlSearchProperties {

    /** maxResults 상한 (FR-016 / SPIKE) */
    private int maxResultsCap = 100;

    /** 기본 페이지 크기 */
    private int defaultMaxResults = 20;
}

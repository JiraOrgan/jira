package com.pch.mng.jql;

import com.pch.mng.issue.IssueResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JqlSearchResponse {

    private int startAt;
    private int maxResults;
    private long total;
    private List<IssueResponse.MinDTO> issues;
}

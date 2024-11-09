package com.ds.deploysurfingbackend.domain.github.dto;

import java.util.List;

public record BranchListDto(
        String name,
        Commit commit,
        boolean isProtected,
        Protection protection,
        String protectionUrl
) {
    public record Commit(
            String sha,
            String url
    ) {}

    public record Protection(
            RequiredStatusChecks requiredStatusChecks
    ) {
        public record RequiredStatusChecks(
                String enforcementLevel,
                List<String> contexts
        ) {}
    }
}

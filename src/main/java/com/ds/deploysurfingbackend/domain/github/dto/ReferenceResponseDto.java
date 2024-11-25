package com.ds.deploysurfingbackend.domain.github.dto;

public record ReferenceResponseDto(
        String ref,
        String nodeId,
        String url,
        GitReferenceObject object
) {
    public record GitReferenceObject(
            String type,
            String sha,
            String url
    ) {}
}



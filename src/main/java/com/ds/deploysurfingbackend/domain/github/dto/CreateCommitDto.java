package com.ds.deploysurfingbackend.domain.github.dto;

import lombok.Builder;

@Builder
public record CreateCommitDto(
        String message,
        String path,
        byte[] content
) {
    public static CreateCommitDto from(String path, byte[] content) {
        return CreateCommitDto.builder()
                .message("Add CI/CD script by Deploy Surfing")
                .path(path)
                .content(content)
                .build();
    }
}

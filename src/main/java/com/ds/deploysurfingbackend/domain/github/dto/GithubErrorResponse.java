package com.ds.deploysurfingbackend.domain.github.dto;

public record GithubErrorResponse(
        String message,
        String documentationUrl,
        String status

) {
}

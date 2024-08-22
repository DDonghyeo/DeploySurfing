package com.ds.deploysurfingbackend.domain.github.dto;

import lombok.Builder;

@Builder
public record ActionSecretDto(
        String name,
        String value
) {
}

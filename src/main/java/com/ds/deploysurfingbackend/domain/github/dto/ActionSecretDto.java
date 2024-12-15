package com.ds.deploysurfingbackend.domain.github.dto;

import lombok.Builder;

@Builder
public record ActionSecretDto(
        String name,
        String value
) {
    public static ActionSecretDto of(String name, String value) {
        return new ActionSecretDto(name, value);
    }
}

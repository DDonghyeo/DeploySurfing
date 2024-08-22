package com.ds.deploysurfingbackend.domain.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record RepositoryPublicKeyResponseDto(
        @JsonProperty("key_id")
        String keyId,
        @JsonProperty("key")
        String key
) {
}

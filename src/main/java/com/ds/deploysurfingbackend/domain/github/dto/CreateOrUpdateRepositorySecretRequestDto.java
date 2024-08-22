package com.ds.deploysurfingbackend.domain.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
public record CreateOrUpdateRepositorySecretRequestDto(
        @JsonProperty("owner")
        String owner,

        @JsonProperty("repo")
        String repo,

        @JsonProperty("secret_name")
        String secretName,

        @JsonProperty("encrypted_value")
        String encryptedValue,

        @JsonProperty("key_id")
        String keyId,

        @JsonProperty("headers")
        Map<String, String> headers

) {
}

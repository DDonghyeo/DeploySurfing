package com.ds.deploysurfingbackend.domain.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
public record CreateOrUpdateRepositorySecretRequestDto(

        @JsonProperty("encrypted_value")
        String encryptedValue,

        @JsonProperty("key_id")
        String keyId

) {
}

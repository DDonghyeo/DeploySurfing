package com.ds.deploysurfingbackend.domain.user.dto.request;

import lombok.Builder;

@Builder
public record JwtDto(
        String accessToken,
        String refreshToken
) {
}

package com.ds.deploysurfingbackend.domain.user.dto.resoonse;

import com.ds.deploysurfingbackend.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserResponseDto(
        String name

) {
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .name(user.getName())
                .build();
    }
}

package com.ds.deploysurfingbackend.domain.app.dto;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppStatus;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppType;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AppDto {

    public record CreateAppDto(
            @NotBlank(message = "[ERROR] 이름은 필수입니다.")
            String name,

            AppType type,

            @NotBlank(message = "[ERROR] GitHub URL은 필수입니다.")
            String gitHubUrl,

            @NotBlank(message = "[ERROR] yml은 필수입니다.")
            String yml

    ) {
        public App toEntity(User user) {
            String url = gitHubUrl;

            return App.builder()
                    .name(name)
                    .type(type==AppType.SPRING? AppType.SPRING : AppType.DJANGO)
                    .status(AppStatus.STARTING) // 초기는 종료 상태
                    .user(user)
                    .build();
        }

    }

    public record UpdateAppDto(
            String name,

            String description
    ) {}

    @Builder
    public record AppResponseDto(
            String id,

            String name,

            AppType type,

            String description,

            AppStatus status,

            String owner,

            String repoName
    ){
        public static AppResponseDto from(App app) {
            return AppResponseDto.builder()
                    .id(app.getId())
                    .name(app.getName())
                    .type(app.getType())
                    .description(app.getDescription())
                    .status(app.getStatus())
                    .build();
        }
    }

}

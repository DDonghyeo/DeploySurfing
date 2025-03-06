package com.ds.deploysurfingbackend.domain.app.dto;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.entity.AppMetadata;
import com.ds.deploysurfingbackend.domain.app.entity.GithubMetaData;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppStatus;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppType;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

@Slf4j
public class AppDto {

    public record CreateAppDto(
            @NotBlank(message = "[ERROR] 이름은 필수입니다.")
            String name,

            AppType type,

            @NotBlank(message = "[ERROR] GitHub URL은 필수입니다.")
            String gitHubUrl,

            // 24.11.21 YML 스프링 부트 전용 -> 다른 프레임워크 적용 대비 제외
            @Nullable
            String yml,

            @Nullable
            String version,

            @Nullable
            String port

    ) {
        public App toEntity(User user) {


            App app = App.builder()
                    .name(name)
                    .type(type == AppType.SPRING ? AppType.SPRING : AppType.DJANGO) //24.11.11 Spring or Django (임시)
                    .status(AppStatus.INITIAL)
                    .user(user)
                    .build();

            AppMetadata metaData = AppMetadata.builder()
                    .version(version)
                    .configFile(yml)
                    .port(port)
                    .build();

            app.setMetaData(metaData);
            return app;
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

            @Nullable
            String description,

            AppStatus status,

            String owner,

            String repoName
    ){
        public static AppResponseDto from(App app, GithubMetaData githubMetaData) {
            return AppResponseDto.builder()
                    .id(app.getId())
                    .name(app.getName())
                    .type(app.getType())
                    .owner(githubMetaData.getOwner())
                    .repoName(githubMetaData.getRepoName())
                    .description(app.getDescription())
                    .status(app.getStatus())
                    .build();
        }
    }

}

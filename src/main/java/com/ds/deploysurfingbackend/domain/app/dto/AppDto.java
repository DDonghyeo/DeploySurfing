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
            Pattern pattern = Pattern.compile("https://github.com/(\\w+)/(\\w+)");
            Matcher matcher = pattern.matcher(url);

            String owner = "";
            String repoName = "";
            if (matcher.find()) {
                owner = matcher.group(1);
                repoName = matcher.group(2);

                log.info(" [ AppDto ] 사용자 이름: {} ", owner);
                log.info(" [ AppDto ] 저장소 이름:  {}" , repoName);
            } else {
                log.info(" [ AppDto ] URL 형식이 올바르지 않습니다.");
                //TODO: Custom Exception
                throw new RuntimeException();
            }

            return App.builder()
                    .name(name)
                    .repoUrl(gitHubUrl)
                    .owner(owner)
                    .repoName(repoName)
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

            String repoUrl,

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
                    .repoUrl(app.getRepoUrl())
                    .owner(app.getOwner())
                    .repoName(app.getRepoName())
                    .build();
        }
    }

}

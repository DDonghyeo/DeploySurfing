package com.ds.deploysurfingbackend.dto;

import com.ds.deploysurfingbackend.domain.App;
import com.ds.deploysurfingbackend.domain.type.AppStatus;
import com.ds.deploysurfingbackend.domain.type.AppType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AppDto {

    @Getter
    public static class createAppDto {

        @NotBlank(message = "[ERROR] 이름은 필수입니다.")
        public String name;

        @NotBlank(message = "[ERROR] 타입은 필수입니다.")
        public AppType type;

        @NotBlank(message = "[ERROR] GitHub URL은 필수입니다.")
        public String gitHubUrl;

        @NotBlank(message = "[ERROR] yml은 필수입니다.")
        public String ymlUrl;

        public Long userId;

        public App toEntity() {
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
                    .status(AppStatus.TERMINATED) // 초기는 종료 상태
                    .userId(userId)
                    .build();
        }

    }

    @Getter
    public static class updateAppDto {

        public String name;

        public String description;
    }

}

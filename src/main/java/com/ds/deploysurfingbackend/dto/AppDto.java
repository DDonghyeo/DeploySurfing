package com.ds.deploysurfingbackend.dto;

import com.ds.deploysurfingbackend.domain.App;
import com.ds.deploysurfingbackend.domain.type.AppStatus;
import com.ds.deploysurfingbackend.domain.type.AppType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class AppDto {

    @Getter
    public static class createAppDto {

        @NotBlank(message = "[ERROR] 이름은 필수입니다.")
        public String name;

        @NotBlank(message = "[ERROR] 타입은 필수입니다.")
        public AppType type;

        public String description;

        public AppStatus status;

        public Long userId;

        public App toEntity() {
            return App.builder()
                    .name(name)
                    .description(description)
                    .status(AppStatus.TERMINATED) // 초기는 종료 상태
                    .build();
        }
    }

    @Getter
    public static class updateAppDto {

        public String name;

        public String description;
    }

}

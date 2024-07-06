package com.ds.deploysurfingbackend.domain.app.entity.type;

import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppType {
    SPRING("SPRING"),
    DJANGO("DJANGO"),
    VANILLA_JS("VANILLA_JS");

    private final String type;

    public String getStatus() {
        return this.type;
    }

    public static AppType of(String type) {
        switch (type) {
            case "SPRING" -> {
                return AppType.SPRING;
            }
            case "DJANGO" -> {
                return AppType.DJANGO;
            }
            case "JS" -> {
                return AppType.VANILLA_JS;
            }
        }
        throw new CustomException(ErrorCode.INVALID_APP_TYPE);
    }
}

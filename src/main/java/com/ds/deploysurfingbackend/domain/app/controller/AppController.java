package com.ds.deploysurfingbackend.domain.app.controller;

import com.ds.deploysurfingbackend.domain.app.dto.AppDto;
import com.ds.deploysurfingbackend.domain.app.service.AppService;
import com.ds.deploysurfingbackend.domain.aws.service.AWSService;
import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app")
public class AppController {

    private final AppService appService;
    private final AWSService awsService;

    @Operation(tags = "app", summary = "앱 생성", description = "앱을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> createApp(
            @CurrentUser AuthUser authUser, @RequestBody @Valid AppDto.CreateAppDto createAppDto) {
        appService.createApp(authUser, createAppDto);
        return ResponseEntity.ok("앱 생성이 완료되었습니다.");
    }

    @Operation(tags = "app", summary = "앱 전체 조회", description = "사용자가 보유한 앱을 모두 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<?> getAppList(@CurrentUser AuthUser authUser) {
        return ResponseEntity.ok(appService.getAppList(authUser));
    }

    @Operation(tags = "app", summary = "앱 조회", description = "앱을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<?> getApp(@CurrentUser AuthUser authUser, @RequestParam("appId") String appId) {
        return ResponseEntity.ok(appService.getApp(authUser, appId));
    }

    @Operation(tags = "app", summary = "앱 삭제", description = "앱을 삭제합니다.")
    @DeleteMapping("")
    public ResponseEntity<?> deleteApp(@CurrentUser AuthUser authUser, @RequestParam("appId") String appId) {
        appService.deleteAppAndTerminateEC2(authUser, appId);
        return ResponseEntity.ok("앱 삭제가 완료되었습니다.");
    }

    @Operation(tags = "app", summary = "앱 수정", description = "앱을 수정합니다. 이름 또는 설명을 바꿉니다. EC2의 이름이 바뀌지는 않습니다. ")
    @PutMapping("/update")
    public ResponseEntity<?> updateApp(@CurrentUser AuthUser authUser,
                                       @RequestParam("appId") String appId,
                                       @RequestBody AppDto.UpdateAppDto updateAppDto ) {
        appService.updateApp(authUser, appId, updateAppDto);
        return ResponseEntity.ok("앱 수정이 완료되었습니다.");
    }

    @Operation(tags = "app", summary = "앱 배포 시작", description = "앱 생성 후, 배포를 시작합니다.")
    @GetMapping("/init")
    public ResponseEntity<?> initApp(@CurrentUser AuthUser authUser, @RequestParam("appId") String appId) {

        appService.initialConfiguration(authUser, appId);
        return ResponseEntity.ok("앱 초기화가 완료되었습니다.");
    }

    @Operation(tags = "app", summary = "앱 일시 중지", description = "앱 실행 중인 EC2를 일시중지합니다.")
    @GetMapping("/init")
    public ResponseEntity<?> pauseApp(@CurrentUser AuthUser authUser, @RequestParam("appId") String appId) {

        appService.pauseApp(authUser, appId);
        return ResponseEntity.ok("앱 일시 중지가 완료되었습니다.");
    }

}

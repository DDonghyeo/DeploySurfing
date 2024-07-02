package com.ds.deploysurfingbackend.domain.app.api;

import com.ds.deploysurfingbackend.domain.app.dto.AppDto;
import com.ds.deploysurfingbackend.domain.app.service.AppService;
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

    @Operation(tags = "app", summary = "앱 생성", description = "앱을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> createApp(@RequestBody @Valid AppDto.createAppDto createAppDto) {

        return ResponseEntity.ok(appService.createApp(createAppDto));
    }

    @Operation(tags = "app", summary = "앱 전체 조회", description = "사용자가 보유한 앱을 모두 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<?> getAppList() {

        return ResponseEntity.ok(appService.getAppList());
    }

    @Operation(tags = "app", summary = "앱 조회", description = "앱을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<?> getApp(@RequestParam("appId") String appId) {

        return ResponseEntity.ok(appService.getApp(appId));
    }

    @Operation(tags = "app", summary = "앱 삭제", description = "앱을 삭제합니다.")
    @DeleteMapping("")
    public ResponseEntity<?> deleteApp(@RequestParam("appId") String appId) {

        return ResponseEntity.ok(appService.deleteApp(appId));
    }

    @Operation(tags = "app", summary = "앱 수정", description = "앱을 수정합니다. 이름 또는 설명을 바꿉니다. EC2의 이름이 바뀌지는 않습니다. ")
    @PutMapping("/update")
    public ResponseEntity<?> updateApp(@RequestParam("appId") String appId,
                                       @RequestBody AppDto.updateAppDto updateAppDto ) {

        return ResponseEntity.ok(appService.updateApp(appId, updateAppDto));
    }

    @Operation(tags = "app", summary = "앱 초기화", description = "앱 생성 직후 시작되는 로직입니다. 앱을 초기화합니다.")
    @GetMapping("/init")
    public ResponseEntity<?> initApp(@RequestParam("appId") String appId) {

        appService.initialConfiguration(appId);
        return ResponseEntity.ok("앱 초기화 완료");
    }

}

package com.ds.deploysurfingbackend.controller;

import com.ds.deploysurfingbackend.dto.AppDto;
import com.ds.deploysurfingbackend.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/app")
public class AppController {

    private final AppService appService;

    @PostMapping("/create")
    public ResponseEntity<?> createApp(@RequestBody @Valid AppDto.createAppDto createAppDto) {

        return ResponseEntity.ok(appService.createApp(createAppDto));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAppList() {

        return ResponseEntity.ok(appService.getAppList());
    }

    @GetMapping("")
    public ResponseEntity<?> getApp(@RequestParam("appId") String appId) {

        return ResponseEntity.ok(appService.getApp(appId));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteApp(@RequestParam("appId") String appId) {

        return ResponseEntity.ok(appService.deleteApp(appId));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateApp(@RequestParam("appId") String appId,
                                       @RequestBody AppDto.updateAppDto updateAppDto ) {

        return ResponseEntity.ok(appService.updateApp(appId, updateAppDto));
    }


}

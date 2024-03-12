package com.ds.deploysurfingbackend.controller;

import com.ds.deploysurfingbackend.dto.AppDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
public class AppController {

    @PostMapping("/create")
    public ResponseEntity<?> createApp(@RequestBody @Valid AppDto.createAppDto createAppDto) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAppList() {
        return ResponseEntity.ok(null);
    }

    @GetMapping("")
    public ResponseEntity<?> getApp(@RequestParam("appId") String appId) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteApp(@RequestParam("appId") String appId) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateApp(@RequestParam("appId") String appId, @RequestBody AppDto.updateAppDto updateAppDto ) {
        return ResponseEntity.ok(null);
    }


}

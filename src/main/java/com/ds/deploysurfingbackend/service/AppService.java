package com.ds.deploysurfingbackend.service;

import com.ds.deploysurfingbackend.domain.App;
import com.ds.deploysurfingbackend.dto.AppDto;
import com.ds.deploysurfingbackend.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;

    //앱 생성
    public ResponseEntity<?> createApp(AppDto.createAppDto createAppDto) {
        App app = createAppDto.toEntity();
        //TODO : User ID Setting
        app.setUserId(1L);
        appRepository.save(app);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //앱 삭제
    public ResponseEntity<?> deleteApp(String appId) {
        //TODO : 삭제할 수 있는 권한이 있는지?
        appRepository.deleteById(appId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //앱 가져오기
    public ResponseEntity<App> getApp(String appId) {
        return ResponseEntity.ok(appRepository.findById(appId));
    }

    //앱 리스트 가져오기
    public ResponseEntity<List<App>> getAppList() {
        //TODO : User ID
        Long userId = 1L;
        return ResponseEntity.ok(appRepository.findAllByUserId(userId));
    }

    //앱 업데이트
    public ResponseEntity<?> updateApp(String appId, AppDto.updateAppDto updateAppDto) {
        App app = appRepository.findById(appId);
        app.update(updateAppDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

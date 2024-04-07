package com.ds.deploysurfingbackend.service;

import com.ds.deploysurfingbackend.domain.App;
import com.ds.deploysurfingbackend.dto.AppDto;
import com.ds.deploysurfingbackend.repository.AppJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AppService {

    private final AppJpaRepository appRepository;

    //앱 생성
    public ResponseEntity<?> createApp(AppDto.createAppDto createAppDto) {
        App app = createAppDto.toEntity();

        //repo Settings


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
        return ResponseEntity.ok(appRepository.findById(appId).orElseThrow());
    }

    //앱 리스트 가져오기
    public ResponseEntity<List<App>> getAppList() {
        //TODO : User ID
        Long userId = 1L;
        return ResponseEntity.ok(appRepository.findAllByUserId(userId));
    }

    //앱 업데이트
    public ResponseEntity<?> updateApp(String appId, AppDto.updateAppDto updateAppDto) {
        App app = appRepository.findById(appId).orElseThrow();
        app.update(updateAppDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    //앱 초기설정하기
    public void initialConfiguration(String appId) {
        App app = appRepository.findById(appId).orElseThrow();

        if (!app.isConfig()) {
            //이미 초기 설정 되어있을 경우
            throw new RuntimeException();
        }

        /**
         * AWS EC2 만들기
         */
        //1. EC2 생성하기

        //2. Public IP 연결하기

        //3.

        /**
         * GitHub 구성하기
         */

        //1. Action Secret 구성하기

        //2. 깃허브 브랜치 만들기 : deploy

        //3. 브랜치에 Dockerfile 생성하기

        //4. 브랜치에 .github/workflows 디렉토리 생성하기

        //5. .github/workflows 디렉토리에 deploy.yml 생성하기
    }
}

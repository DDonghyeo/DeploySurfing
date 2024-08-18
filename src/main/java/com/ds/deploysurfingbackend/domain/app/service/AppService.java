package com.ds.deploysurfingbackend.domain.app.service;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.exception.AppErrorCode;
import com.ds.deploysurfingbackend.domain.aws.service.AWSService;
import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.app.dto.AppDto;
import com.ds.deploysurfingbackend.domain.app.repository.AppRepository;
import com.ds.deploysurfingbackend.domain.user.exception.UserErrorCode;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final AWSService awsService;

    //앱 생성
    @Transactional
    public void createApp(AuthUser authUser, AppDto.CreateAppDto createAppDto) {

        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        App app = createAppDto.toEntity(user);

        //repo Settings
//        GitHubPublicKeyDto publicKeyDto = GitHubUtils.getRepositoryPublicKey(app, user.getGitHubToken());
//        app.setRepoPublicKeyId(publicKeyDto.getKey_id());
//        app.setRepoPublicKey(publicKeyDto.getKey());

        appRepository.save(app);
    }

    //앱 삭제
    @Transactional
    public void deleteAppAndTerminateEC2(AuthUser authUser, String appId) {

        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND)
        );
        //삭제할 수 있는 권한 있는지 확인
        checkAppAccessPermission(authUser.getEmail(), app);

        appRepository.deleteById(appId);

        awsService.terminateEC2(authUser, app.getEc2().getEc2Id());
    }

    //앱 가져오기
    @Transactional(readOnly = true)
    public AppDto.AppResponseDto getApp(AuthUser authUser, String appId) {

        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermission(authUser.getEmail(), app);

        return AppDto.AppResponseDto.from(app);
    }

    //앱 리스트 가져오기
    @Transactional
    public List<AppDto.AppResponseDto> getAppList(AuthUser authUser) {
        return appRepository.findAllByUserId(authUser.getId())
                        .stream()
                        .map(AppDto.AppResponseDto::from)
                        .toList();
    }

    //앱 업데이트
    @Transactional
    public void updateApp(AuthUser authUser, String appId, AppDto.UpdateAppDto updateAppDto) {

        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermission(authUser.getEmail(), app);

        app.update(updateAppDto);
    }


    //앱 초기 설정
    @Transactional
    public void initialConfiguration(AuthUser authUser, String appId) {

        log.info("[ App Service ] 앱 초기 설정을 시작합니다. ---> {}", appId);
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        checkAppInitialized(app);
        checkAppAccessPermission(user.getEmail(), app);

        //1. EC2 생성
        awsService.createEC2(authUser, app.getName());

        /**
         * GitHub 구성하기
         */

        //1. Action Secret 구성하기
        //APPLICATION_YML
        //DOCKERHUB_IMAGENAME
        //DOCKERHUB_TOKEN
        //DOCKERHUB_USERNAME
        //EC2_HOST
        //EC2_PASSWORD
        //EC2_SSH_PORT
        //EC2_USERNAME
        //SSL_KEY

        //2. 깃허브 브랜치 만들기 : deploy

        //3. 브랜치에 Dockerfile 생성하기

        //4. 브랜치에 .github/workflows 디렉토리 생성하기

        //5. .github/workflows 디렉토리에 deploy.yml 생성하기

        //앱 초기설정 완료
        app.setInit(true);
    }

    private void checkAppAccessPermission(String email, App app) {
        if (!app.getUser().getEmail().equals(email))
            throw new CustomException(CommonErrorCode.NO_AUTHORIZED);
    }

    private void checkAppInitialized(App app) {
        if (!app.isInit()) {
            //이미 초기 설정 되어있을 경우
            log.warn("[ App Service ] 이미 초기 설정이 실행된 앱입니다. ---> {}", app.getId());
            throw new CustomException(AppErrorCode.APP_ALREADY_INITIALIZED);
        }
    }
}

package com.ds.deploysurfingbackend.domain.app.service;

import com.ds.deploysurfingbackend.domain.app.dto.GitHubPublicKeyDto;
import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.entity.GithubMetaData;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppStatus;
import com.ds.deploysurfingbackend.domain.app.exception.AppErrorCode;
import com.ds.deploysurfingbackend.domain.app.repository.GithubMetadataRepository;
import com.ds.deploysurfingbackend.domain.aws.service.AWSService;
import com.ds.deploysurfingbackend.domain.github.dto.ActionSecretDto;
import com.ds.deploysurfingbackend.domain.github.dto.RepositoryPublicKeyResponseDto;
import com.ds.deploysurfingbackend.domain.github.service.GitHubService;
import com.ds.deploysurfingbackend.domain.github.utils.GitHubUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppService {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("https://github.com/(\\w+)/(\\w+)");
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final GithubMetadataRepository githubMetadataRepository;
    private final AWSService awsService;
    private final GitHubService gitHubService;

    //앱 생성
    @Transactional
    public void createApp(AuthUser authUser, AppDto.CreateAppDto createAppDto) {


        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        App app = createAppDto.toEntity(user);

        //repo Settings
        String repoName = getRepoName(createAppDto.gitHubUrl());
        String owner = createAppDto.gitHubUrl();

        RepositoryPublicKeyResponseDto repositoryPublicKey
                = GitHubUtils.getRepositoryPublicKey(owner, repoName, user.getGitHubToken());


        githubMetadataRepository.save(GithubMetaData.builder()
                .owner(owner)
                .repoName(repoName)
                .repoPublicKeyId(repositoryPublicKey.keyId())
                .repoPublicKey(repositoryPublicKey.key())
                .app(app)
                .repoUrl(createAppDto.gitHubUrl())
                .build());

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
        //TODO: EC2 생성되어 있는지 확인 필요
        app.setStatus(AppStatus.STARTING);
        awsService.createEC2(authUser, app.getName());

        //1. Action Secret 구성하기
        List<ActionSecretDto> secrets = createDefaultActionSecrets();
        secrets.forEach(secret -> gitHubService.createActionSecret(authUser, appId, user.getGitHubToken(), secret));


        //2. 깃허브 브랜치 만들기 : deploy

        //3. 브랜치에 Dockerfile 생성하기

        //4. 브랜치에 .github/workflows 디렉토리 생성하기

        //5. .github/workflows 디렉토리에 deploy.yml 생성하기

        //앱 초기설정 완료
        app.setStatus(AppStatus.RUNNING);
        app.setInit(true);
    }

    private List<ActionSecretDto> createDefaultActionSecrets() {
        return Arrays.asList(
                new ActionSecretDto("APPLICATION_YML", "temp"),
                new ActionSecretDto("DOCKERHUB_IMAGENAME", "temp"),
                new ActionSecretDto("DOCKERHUB_TOKEN", "temp"),
                new ActionSecretDto("DOCKERHUB_USERNAME", "temp"),
                new ActionSecretDto("EC2_HOST", "temp"),
                new ActionSecretDto("SSH_KEY", "temp")
        );
    }

    @Transactional
    public void pauseApp(AuthUser authUser, String appId) {
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermission(authUser.getEmail(), app);

        awsService.pauseEC2(authUser, app.getEc2().getEc2Id());

        app.setStatus(AppStatus.PAUSED);
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

    private String getOwner(String githubUrl) {
        return parseGitHubUrl(githubUrl)[0];
    }

    private String getRepoName(String githubUrl) {
        return parseGitHubUrl(githubUrl)[1];
    }

    private String[] parseGitHubUrl(String githubUrl) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(githubUrl);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repoName = matcher.group(2);

            log.info(" [ AppService ] 사용자 이름: {} ", owner);
            log.info(" [ AppService ] 저장소 이름: {} ", repoName);

            return new String[]{owner, repoName};
        } else {
            log.error(" [ AppService ] URL 형식이 올바르지 않습니다: {}", githubUrl);
            throw new IllegalArgumentException("유효하지 않은 GitHub URL 형식입니다.");
        }
    }
}

package com.ds.deploysurfingbackend.domain.app.service;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.entity.GithubMetaData;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppStatus;
import com.ds.deploysurfingbackend.domain.app.exception.AppErrorCode;
import com.ds.deploysurfingbackend.domain.app.repository.GithubMetadataRepository;
import com.ds.deploysurfingbackend.domain.aws.entity.EC2;
import com.ds.deploysurfingbackend.domain.aws.repository.EC2Repository;
import com.ds.deploysurfingbackend.domain.aws.service.AwsService;
import com.ds.deploysurfingbackend.domain.github.dto.ActionSecretDto;
import com.ds.deploysurfingbackend.domain.github.dto.RepositoryPublicKeyResponseDto;
import com.ds.deploysurfingbackend.domain.github.exception.GithubErrorCode;
import com.ds.deploysurfingbackend.domain.github.service.GitHubService;
import com.ds.deploysurfingbackend.domain.github.utils.GitHubApiClient;
import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.app.dto.AppDto;
import com.ds.deploysurfingbackend.domain.app.repository.AppRepository;
import com.ds.deploysurfingbackend.domain.user.exception.UserErrorCode;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.utils.FileReaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppService {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("https://github.com/([\\w-]+)/([\\w-]+)");
    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final GithubMetadataRepository githubMetadataRepository;
    private final AwsService awsService;
    private final GitHubService gitHubService;
    private final GitHubApiClient gitHubApiClient;
    private final FileReaderUtil fileReader;

    //앱 생성
    @Transactional
    public void createApp(final AuthUser authUser, final AppDto.CreateAppDto createAppDto) {


        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        App app = createAppDto.toEntity(user);

        //repo Settings
        String[] githubUrlData = parseGitHubUrl(createAppDto.gitHubUrl());
        String owner = githubUrlData[0];
        String repoName = githubUrlData[1];

        //레포 Public Key 획득
        RepositoryPublicKeyResponseDto repositoryPublicKey
                = gitHubApiClient.getRepositoryPublicKey(owner, repoName, user.getGitHubToken());

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

    //앱 삭제 + EC2 종료
    @Transactional
    public void deleteAppAndTerminateEC2(AuthUser authUser, String appId) {

        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND)
        );
        //삭제할 수 있는 권한 있는지 확인
        checkAppAccessPermissionByEmail(authUser.getEmail(), app);

        appRepository.deleteById(appId);

        awsService.terminateEC2(authUser, app.getEc2().getEc2Id());
    }

    //앱 가져오기
    @Transactional(readOnly = true)
    public AppDto.AppResponseDto getApp(AuthUser authUser, String appId) {

        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        GithubMetaData githubMetaData = githubMetadataRepository.findByApp_Id(appId).orElseThrow(
                () -> new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));

        checkAppAccessPermissionByEmail(authUser.getEmail(), app);

        return AppDto.AppResponseDto.from(app, githubMetaData);
    }

    //앱 리스트 가져오기
    @Transactional
    public List<AppDto.AppResponseDto> getAppList(AuthUser authUser) {
        return appRepository.findAllByUserId(authUser.getId())
                        .stream()
                        .map((app) ->{
                            GithubMetaData githubMetaData = githubMetadataRepository.findByApp_Id(app.getId()).orElseThrow(
                                    () -> new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));
                            return AppDto.AppResponseDto.from(app, githubMetaData);
                        })
                        .toList();
    }

    //앱 업데이트
    @Transactional
    public void updateApp(AuthUser authUser, String appId, AppDto.UpdateAppDto updateAppDto) {

        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermissionByEmail(authUser.getEmail(), app);

        app.update(updateAppDto);
    }


    //앱 초기 설정
    @Transactional
    public void initialConfiguration(AuthUser authUser, String appId) {

        log.info("[ App Service ] 앱 초기 설정을 시작합니다. ---> {}", appId);
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        log.info(authUser.getEmail());
        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        GithubMetaData githubMetaData = githubMetadataRepository.findByApp_Id(appId).orElseThrow(
                () -> new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND)
        );

        checkAppInitialized(app);
        checkAppAccessPermissionByEmail(user.getEmail(), app);

        //1. EC2 생성
        //TODO: EC2 생성되어 있는지 확인 필요
        app.setStatus(AppStatus.EC2_CREATION_STARTED);
        EC2 ec2 = awsService.createEC2(authUser, app.getName());
        app.setStatus(AppStatus.EC2_CREATION_COMPLETED);


        //2. Action Secret 구성하기
        app.setStatus(AppStatus.ACTION_SECRET_CONFIGURATION_IN_PROGRESS);
        List<ActionSecretDto> secrets = createSecretAction(user, app, ec2);
        secrets.forEach(secret -> gitHubService.createActionSecret(githubMetaData, user.getGitHubToken(), secret));


        //2. 깃허브 브랜치 만들기 : deploy
        app.setStatus(AppStatus.DEPLOY_BRANCH_CREATION_IN_PROGRESS);
        gitHubService.createBranch(githubMetaData, "deploy", user.getGitHubToken());

        //3. Deploy 브랜치에 Dockerfile 생성하기
        app.setStatus(AppStatus.DOCKERFILE_CREATION_IN_PROGRESS);
        gitHubService.createDockerfile(githubMetaData, user.getGitHubToken(), "17");

        //4. .github/workflows 디렉토리에 deploy.yml 생성하기
        gitHubService.createCICDScript(githubMetaData, user.getGitHubToken());

        //앱 초기설정 완료
        completeInitialization(app);
    }

    @Transactional
    public void pauseApp(AuthUser authUser, String appId) {
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermissionByEmail(authUser.getEmail(), app);

        awsService.pauseEC2(authUser, app.getEc2().getEc2Id());

        app.setStatus(AppStatus.PAUSED);
    }

    private List<ActionSecretDto> createSecretAction(User user, App app, EC2 ec2) {
        return Arrays.asList(
                ActionSecretDto.of("APPLICATION_YML", app.getMetaData().getConfigFile()),
                ActionSecretDto.of("DOCKERHUB_USERNAME", user.getDockerHubName()),
                ActionSecretDto.of("DOCKERHUB_TOKEN", user.getDockerToken()),
                ActionSecretDto.of("DOCKERHUB_IMAGENAME", app.getName().toLowerCase()),
                ActionSecretDto.of("EC2_HOST", ec2.getPublicIp()),
                ActionSecretDto.of("EC2_USERNAME", "ec2-user"),
                ActionSecretDto.of("EC2_PRIVATE_KEY", fileReader.readFileAsString(ec2.getKeyFilePath())),
                ActionSecretDto.of("APP_PORT", app.getMetaData().getPort()) //임시

        );
    }
    private void checkAppInitialized(App app) {
        if (app.isInit()) {
            //이미 초기 설정 되어있을 경우
            log.warn("[ App Service ] 이미 초기 설정이 실행된 앱입니다. ---> {}", app.getId());
            throw new CustomException(AppErrorCode.APP_ALREADY_INITIALIZED);
        }
    }

    // Github Repo URL 파싱, [Owner, Name] 반환
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
            throw new CustomException(GithubErrorCode.INFORMAL_GITHUB_URL);
        }
    }

    private void checkAppAccessPermissionByEmail(String email, App app) {
        if (!app.getUser().getEmail().equals(email))
            throw new CustomException(CommonErrorCode.NO_AUTHORIZED);
    }

    private void checkAppAccessPermissionById(Long userId, App app) {
        if (!app.getUser().getId().equals(userId))
            throw new CustomException(CommonErrorCode.NO_AUTHORIZED);
    }

    private void completeInitialization(App app) {
        app.setStatus(AppStatus.RUNNING);
        app.setInit(true);
    }
}

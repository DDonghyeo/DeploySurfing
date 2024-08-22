package com.ds.deploysurfingbackend.domain.github.service;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.exception.AppErrorCode;
import com.ds.deploysurfingbackend.domain.app.repository.AppRepository;
import com.ds.deploysurfingbackend.domain.github.dto.ActionSecretDto;
import com.ds.deploysurfingbackend.domain.github.dto.CreateCommitDto;
import com.ds.deploysurfingbackend.domain.github.dto.RepositoryPublicKeyResponseDto;
import com.ds.deploysurfingbackend.domain.github.utils.GitHubUtils;
import com.ds.deploysurfingbackend.domain.github.utils.SodiumUtils;
import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.exception.UserErrorCode;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.utils.YamlFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class GitHubService {

    private final YamlFileReader yamlFileReader;
    private final AppRepository appRepository;
    private final UserRepository userRepository;

    public void createCICDScript(AuthUser authUser, String appId) {
        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermission(authUser.getId(), app);

        //현재는 생성만 됨. 수정은 sha 필드가 추가로 필요
        String owner = app.getOwner();
        String repo = app.getRepoName();
        String token = user.getGitHubToken();
        
        //TODO: 앱 스펙에 맞게 맞는 스크립트로 가져오기
        byte[] content = yamlFileReader.readYamlFileAsBytes("cicd.yml");

        String fileName = "cicd.yml";
        String path = "/.github/workflows/"+fileName;

        GitHubUtils.createFileContents(token, owner, repo, CreateCommitDto.from(path, content));
    }


    public void createActionSecret(AuthUser authUser, String appId, String gitHubToken, ActionSecretDto actionSecret) {
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermission(authUser.getId(), app);

        //Repository Public Key & Key Id
        RepositoryPublicKeyResponseDto repositoryPublicKey = getRepositoryPublicKey(app, gitHubToken);

        // LibSodium encrypt
        String encryptedSecretValue = SodiumUtils.encryptSecret(repositoryPublicKey.key(), actionSecret.value());

        //Repository Secret 업데이트
        GitHubUtils.createOrUpdateRepositorySecret(app.getOwner(), app.getRepoName(), gitHubToken,
                actionSecret.name(), encryptedSecretValue);
    }

    private RepositoryPublicKeyResponseDto getRepositoryPublicKey(App app, String gitHubToken) {
        if (app.getRepoPublicKey() == null) {
            return GitHubUtils.getRepositoryPublicKey(app.getOwner(), app.getRepoName(), gitHubToken);
        } else return RepositoryPublicKeyResponseDto.builder()
                .key(app.getRepoPublicKey())
                .keyId(app.getRepoPublicKeyId())
                .build();
    }

    private void checkAppAccessPermission(Long userId, App app) {
        if (!app.getUser().getId().equals(userId))
            throw new CustomException(CommonErrorCode.NO_AUTHORIZED);
    }

}

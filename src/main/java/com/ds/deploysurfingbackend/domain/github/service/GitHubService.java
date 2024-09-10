package com.ds.deploysurfingbackend.domain.github.service;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.app.entity.GithubMetaData;
import com.ds.deploysurfingbackend.domain.app.exception.AppErrorCode;
import com.ds.deploysurfingbackend.domain.app.repository.AppRepository;
import com.ds.deploysurfingbackend.domain.app.repository.GithubMetadataRepository;
import com.ds.deploysurfingbackend.domain.github.dto.ActionSecretDto;
import com.ds.deploysurfingbackend.domain.github.dto.CreateCommitDto;
import com.ds.deploysurfingbackend.domain.github.dto.RepositoryPublicKeyResponseDto;
import com.ds.deploysurfingbackend.domain.github.exception.GithubErrorCode;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class GitHubService {

    private final YamlFileReader yamlFileReader;
    private final AppRepository appRepository;
    private final UserRepository userRepository;

    private final GithubMetadataRepository githubMetadataRepository;

    public void createCICDScript(AuthUser authUser, String appId) {
        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        GithubMetaData githubMetaData = githubMetadataRepository.findByApp_Id(appId).orElseThrow(
                () -> new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));
        App app = appRepository.findById(appId).orElseThrow(
                () -> new CustomException(AppErrorCode.APP_NOT_FOUND));

        checkAppAccessPermission(authUser.getId(), app);

        //현재는 생성만 됨. 수정은 sha 필드가 추가로 필요
        String owner = githubMetaData.getOwner();
        String repo = githubMetaData.getRepoName();
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
        GithubMetaData githubMetaData = githubMetadataRepository.findByApp_Id(appId).orElseThrow(
                () -> new CustomException(GithubErrorCode.GITHUB_METADATA_NOT_FOUND));

        checkAppAccessPermission(authUser.getId(), app);

        // Repository Public Key & Key Id
        RepositoryPublicKeyResponseDto repositoryPublicKey = getRepositoryPublicKey(githubMetaData, gitHubToken);

        // LibSodium encrypt
        String encryptedSecretValue = SodiumUtils.encryptSecret(repositoryPublicKey.key(), actionSecret.value());

        //Repository Secret 업데이트
        GitHubUtils.createOrUpdateRepositorySecret(githubMetaData.getOwner(), githubMetaData.getRepoName(), gitHubToken,
                actionSecret.name(), encryptedSecretValue);
    }

    private RepositoryPublicKeyResponseDto getRepositoryPublicKey(GithubMetaData githubMetaData, String gitHubToken) {
        if (githubMetaData.getRepoPublicKey() == null) {
            return GitHubUtils.getRepositoryPublicKey(githubMetaData.getOwner(), githubMetaData.getRepoName(), gitHubToken);
        } else return RepositoryPublicKeyResponseDto.builder()
                .key(githubMetaData.getRepoPublicKey())
                .keyId(githubMetaData.getRepoPublicKeyId())
                .build();
    }

    private void checkAppAccessPermission(Long userId, App app) {
        if (!app.getUser().getId().equals(userId))
            throw new CustomException(CommonErrorCode.NO_AUTHORIZED);
    }

}

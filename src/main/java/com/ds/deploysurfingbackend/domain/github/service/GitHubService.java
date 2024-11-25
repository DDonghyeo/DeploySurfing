package com.ds.deploysurfingbackend.domain.github.service;

import com.ds.deploysurfingbackend.domain.app.entity.GithubMetaData;
import com.ds.deploysurfingbackend.domain.github.dto.ActionSecretDto;
import com.ds.deploysurfingbackend.domain.github.dto.CreateCommitDto;
import com.ds.deploysurfingbackend.domain.github.dto.RepositoryPublicKeyResponseDto;
import com.ds.deploysurfingbackend.domain.github.exception.GithubErrorCode;
import com.ds.deploysurfingbackend.domain.github.utils.GitHubApiClient;
import com.ds.deploysurfingbackend.domain.github.utils.SodiumUtils;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.utils.FileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GitHubService {

    private final String DEPLOY_BRANCH_NAME = "deploy";

    private final FileReader fileReader;
    private final GitHubApiClient gitHubApiClient;

    public void createCICDScript(GithubMetaData githubMetaData, String token) {

        //현재는 생성만 됨. 수정은 sha 필드가 추가로 필요
        //TODO: 앱 스펙에 맞게 맞는 스크립트로 가져오기
        byte[] content = fileReader.readFileAsBytes("cicd.yml");

        String fileName = "cicd.yml";
        String path = "/.github/workflows/"+fileName;

        gitHubApiClient.createFileContents(token, githubMetaData.getOwner(), githubMetaData.getRepoName(), DEPLOY_BRANCH_NAME,  CreateCommitDto.from(path, content));
    }

    public void createDockerfile(GithubMetaData githubMetaData, String token, String javaVersion) {

        //TODO : 앱 스펙에 맞게 가져오기, 현재는 Java 17 (24.11.25)
        byte[] content = fileReader.readFileAsBytes("Dockerfile");

        String fileName = "Dockerfile";
        String path = "/"+fileName;
        gitHubApiClient.createFileContents(token, githubMetaData.getOwner(), githubMetaData.getRepoName(), DEPLOY_BRANCH_NAME,  CreateCommitDto.from(path, content));
    }


    public void createActionSecret(GithubMetaData githubMetaData, String gitHubToken, ActionSecretDto actionSecret) {

        // Repository Public Key & Key ID
        RepositoryPublicKeyResponseDto repositoryPublicKey = getRepositoryPublicKey(githubMetaData, gitHubToken);

        // LibSodium encrypt
        String encryptedSecretValue = SodiumUtils.encryptSecret(repositoryPublicKey.key(), actionSecret.value());

        //Repository Secret 업데이트
        gitHubApiClient.createOrUpdateRepositorySecret(githubMetaData.getOwner(), githubMetaData.getRepoName(), gitHubToken,
                actionSecret.name(), encryptedSecretValue);
    }

    /**
     *  브랜치를 생성합니다.
     *
     * @throws CustomException  생성하려는 브랜치가 이미 존재할 경우
     */
    public void createBranch(GithubMetaData githubMetaData, String branch, String githubToken) {
        //해당 브랜치가 이미 있는지 검사
        if (gitHubApiClient.listBranches(githubMetaData.getOwner(), githubMetaData.getRepoName(), githubToken).stream()
                .anyMatch(branchListDto -> branchListDto.name().equals(branch))) {
            throw new CustomException(GithubErrorCode.DEPLOY_BRANCH_ALREADY_EXISTS);
        }
    }

    private RepositoryPublicKeyResponseDto getRepositoryPublicKey(GithubMetaData githubMetaData, String gitHubToken) {

        if (githubMetaData.getRepoPublicKey() == null) {
            return gitHubApiClient.getRepositoryPublicKey(githubMetaData.getOwner(), githubMetaData.getRepoName(), gitHubToken);
        } else return RepositoryPublicKeyResponseDto.builder()
                .key(githubMetaData.getRepoPublicKey())
                .keyId(githubMetaData.getRepoPublicKeyId())
                .build();
    }

}

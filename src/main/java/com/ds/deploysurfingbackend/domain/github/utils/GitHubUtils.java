package com.ds.deploysurfingbackend.domain.github.utils;

import com.ds.deploysurfingbackend.domain.github.dto.*;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class GitHubUtils {

    private static final String GITHUB_API_URL = "https://api.github.com/";

    private static final String DS_EMAIL = "ds@gmail.com";
    private static final String DS_COMMITTER = "Deploy Surfing";

    /**
     * <h1>Create or update file contents</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/repos/contents?apiVersion=2022-11-28#create-or-update-file-contents">Link</a> <br>
     * /repos/{owner}/{repo}/contents/{path}
     */
    public static void createFileContents(
            String token,
            String owner,
            String repo,
            String branch,
            CreateCommitDto createCommitDto) {
        log.info("[ GitHubUtils ] File Commit started ...");
        CreateCommitRequestDto commitRequestDto = CreateCommitRequestDto.builder()
                .owner(owner)
                .repo(repo)
                .message(createCommitDto.message())
                .committer(
                        CreateCommitRequestDto.Committer.builder()
                                .email(DS_EMAIL)
                                .name(DS_COMMITTER)
                                .build()
                )
                .branch(branch)
                .path(createCommitDto.path())
                .content(Base64.getEncoder().encodeToString(createCommitDto.content()))
                .headers(Map.of("X-GitHub-Api-Version", "2022-11-28"))
                .build();

        WebClient.create(GITHUB_API_URL)
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos")
                        .path("/"+owner)
                        .path("/"+repo)
                        .path("/contents")
                        .path(createCommitDto.path())
                        .build()
                )
                .header("Authorization", "Bearer " +token)
                .body(Mono.just(commitRequestDto), CreateCommitRequestDto.class)
                .retrieve()
                //200 OK
                //201 Created
                //404 Resource not found
                //409 Conflict
                //422 Validation failed, or the endpoint has been spammed.
                .onStatus(HttpStatusCode::is2xxSuccessful, clientResponse -> Mono.error(new CustomException(CommonErrorCode.SERVER_ERROR)))
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new CustomException(CommonErrorCode.SERVER_ERROR)))
                .toBodilessEntity().block();
    }

    /**
     * <h1>Get a repository public key</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/actions/secrets?apiVersion=2022-11-28#get-a-repository-public-key">Link</a> <br>
     * /repos/{owner}/{repo}/actions/secrets/public-key
     */
    public static RepositoryPublicKeyResponseDto getRepositoryPublicKey(final String owner, final String repo, final String token) {
        log.info("[ GitHubUtils ] Getting Repository Public Key ...");
        return WebClient.create(GITHUB_API_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("repos", owner, repo, "actions", "secrets", "public-key")
                        .build()
                )
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(RepositoryPublicKeyResponseDto.class)
                .block();
    }

    /**
     * <h1>Create or update a repository secret</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/actions/secrets?apiVersion=2022-11-28#create-or-update-a-repository-secret">Link</a> <br>
     * /repos/{owner}/{repo}/actions/secrets/{secret_name}
     */
    public static ResponseEntity<?> createOrUpdateRepositorySecret(
            final String owner, final String repo, final String token,
            final String secretName, final String encryptedSecretValue) {

        log.info("[ GitHubUtils ] Create or update repository secret ---> {}", secretName);
        CreateOrUpdateRepositorySecretRequestDto requestDto = CreateOrUpdateRepositorySecretRequestDto.builder()
                .owner(owner)
                .repo(repo)
                .secretName(secretName)
                .encryptedValue(encryptedSecretValue)
                .headers(Map.of("X-GitHub-Api-Version", "2022-11-28"))
                .build();

        return WebClient.create(GITHUB_API_URL)
                .put()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("repos", owner, repo, "actions", "secrets", secretName)
                        .build()
                )
                .header("Authorization", "Bearer " + token)
                .body(Mono.just(requestDto), CreateOrUpdateRepositorySecretRequestDto.class)
                .retrieve()
                //201 : Created
                //204 : Updated
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new CustomException(CommonErrorCode.SERVER_ERROR)))
                .toBodilessEntity().block();
    }

    /**
     * <h1>List Branches</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/branches/branches?apiVersion=2022-11-28#list-branches">Link</a> <br>
     * /repos/{owner}/{repo}/branches
     */
    public static List<BranchListDto> listBranches(final String owner, final String repo, final String token) {

        return WebClient.create(GITHUB_API_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("repos", owner, repo, "branches")
                        .build()
                )
                .header("Authorization", "Bearer " + token)
                .retrieve()
                //201 : Created
                //204 : Updated
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new CustomException(CommonErrorCode.SERVER_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<List<BranchListDto>>() {
                })
                .block();
    }

}

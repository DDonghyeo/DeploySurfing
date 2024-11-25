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
public class GitHubApiClient {

    private final String GITHUB_API_URL = "https://api.github.com/";

    private final String DS_EMAIL = "ds@gmail.com";
    private final String DS_COMMITTER = "Deploy Surfing";
    private final Map<String, String> GITHUB_VERSION_HEADER = Map.of("X-GitHub-Api-Version", "2022-11-28");

    private final WebClient githubWebClient;

    /**
     * Create or update file contents
     * @see <a href= "https://docs.github.com/ko/rest/repos/contents?apiVersion=2022-11-28#create-or-update-file-contents">Link</a> <br>
     * @path /repos/{owner}/{repo}/contents/{path}
     */
    public void createFileContents(
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
                .headers(GITHUB_VERSION_HEADER)
                .build();


        githubWebClient.put()
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
                .toBodilessEntity()
                .block();
    }

    /**
     * Get a repository public key
     * @see <a href= "https://docs.github.com/ko/rest/actions/secrets?apiVersion=2022-11-28#get-a-repository-public-key">Link</a> <br>
     * @path /repos/{owner}/{repo}/actions/secrets/public-key
     */
    public RepositoryPublicKeyResponseDto getRepositoryPublicKey(final String owner, final String repo, final String token) {
        log.info("[ GitHubUtils ] Getting Repository Public Key ...");
        return githubWebClient
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
     * Create or update a repository secret
     * @see <a href= "https://docs.github.com/ko/rest/actions/secrets?apiVersion=2022-11-28#create-or-update-a-repository-secret">Link</a> <br>
     * @path /repos/{owner}/{repo}/actions/secrets/{secret_name}
     */
    public void createOrUpdateRepositorySecret(
            final String owner, final String repo, final String token,
            final String secretName, final String encryptedSecretValue) {

        log.info("[ GitHubUtils ] Create or update repository secret ---> {}", secretName);

        CreateOrUpdateRepositorySecretRequestDto requestDto = CreateOrUpdateRepositorySecretRequestDto.builder()
                .owner(owner)
                .repo(repo)
                .secretName(secretName)
                .encryptedValue(encryptedSecretValue)
                .headers(GITHUB_VERSION_HEADER)
                .build();

        githubWebClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("repos", owner, repo, "actions", "secrets", secretName)
                        .build()
                )
                .header("Authorization", "Bearer " + token)
                .body(Mono.just(requestDto), CreateOrUpdateRepositorySecretRequestDto.class)
                .retrieve()
                .toBodilessEntity().block();
    }

    /**
     * List Branches
     * @see <a href= "https://docs.github.com/ko/rest/branches/branches?apiVersion=2022-11-28#list-branches">Link</a> <br>
     * @path /repos/{owner}/{repo}/branches
     */
    public List<BranchListDto> listBranches(final String owner, final String repo, final String token) {

        return githubWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("repos", owner, repo, "branches")
                        .build()
                )
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BranchListDto>>() {
                })
                .block();
    }

    /**
     * Create a branch
     * @see <a href= "https://docs.github.com/en/rest/git/refs#create-a-reference">Link</a> <br>
     * @path /repos/{owner}/{repo}/git/refs
     */
    public void createBranch(
            final String owner,
            final String repo,
            final String branch,
            final String token) {

        //sha 추출
        ReferenceResponseDto reference = githubWebClient
                .get()
                .uri("/repos/{owner}/{repo}/git/refs/heads/main", owner, repo)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(ReferenceResponseDto.class)
                .block();

        //브랜치 생성
        CreateBranchRequestDto requestDto = new CreateBranchRequestDto(
                "refs/heads/" + branch,
                reference.object().sha()
        );

        githubWebClient
                .post()
                .uri("/repos/{owner}/{repo}/git/refs", owner, repo)
                .header("Authorization", token)
                .body(Mono.just(requestDto), CreateBranchRequestDto.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}

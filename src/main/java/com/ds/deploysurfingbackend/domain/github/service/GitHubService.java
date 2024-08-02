package com.ds.deploysurfingbackend.domain.github.service;

import com.ds.deploysurfingbackend.domain.app.dto.GitHubPublicKeyDto;
import com.ds.deploysurfingbackend.domain.app.entity.App;
import com.ds.deploysurfingbackend.domain.github.dto.CreateCommitRequestDto;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import com.ds.deploysurfingbackend.global.utils.SodiumUtils;
import com.ds.deploysurfingbackend.global.utils.YamlFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class GitHubService {

    private final String GITHUB_API_URL = "https://api.github.com/";

    private final String DS_EMAIL = "ds@gmail.com";
    private final String DS_COMMITTER = "Deploy Surfing";

    private final YamlFileReader yamlFileReader;


    /**
     * <h1>Create or update file contents</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/repos/contents?apiVersion=2022-11-28#create-or-update-file-contents">Link</a> <br>
     * /repos/{owner}/{repo}/contents/{path}
     */

    public void createCommit(App app, User user) {
        //현재는 생성만 됨. 수정은 sha 필드가 추가로 필요
        String owner = app.getOwner();
        String repo = app.getRepoName();
        String token = user.getGitHubToken();
        
        //TODO: 앱 스펙에 맞게 맞는 스크립트로 가져오기
        byte[] content = yamlFileReader.readYamlFileAsBytes("cicd.yml");

        //TODO : Path 수정
        String path = "/.github/workflows/cicd.yml";
        String message = "Add CI/CD yml by Deploy Surfing";

        CreateCommitRequestDto commitRequestDto = CreateCommitRequestDto.builder()
                .owner(owner)
                .repo(repo)
                .message(message)
                .committer(
                        CreateCommitRequestDto.Committer.builder()
                                .email(DS_EMAIL)
                                .name(DS_COMMITTER)
                                .build()
                )
                .path(path)
                .content(Base64.getEncoder().encodeToString(content))
                .headers(Map.of("X-GitHub-Api-Version", "2022-11-28"))
                .build();

        //when
        ResponseEntity<Void> response = WebClient.create(GITHUB_API_URL)
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos")
                        .path("/"+owner)
                        .path("/"+repo)
                        .path("/contents")
                        .path(path)
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
                .onStatus(HttpStatusCode::is2xxSuccessful, clientResponse -> Mono.error(new CustomException(ErrorCode.SERVER_ERROR)))
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new CustomException(ErrorCode.SERVER_ERROR)))
                .toBodilessEntity().block();

    }


    /**
     * <h1>Create Action Secret</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/actions/secrets?apiVersion=2022-11-28#create-or-update-a-repository-secret">Link</a> <br>
     * /repos/{owner}/{repo}/actions/secrets/{secret_name}
     */

    public static void createActionSecret(App app, String gitHubToken, String secretName, String secretValue ) {

        String url = "https://api.github.com/"
                + "repos" + "/"
                + app.getOwner() + "/"
                + app.getRepoName() + "/"
                + "actions" + "/"
                + "secrets" + "/"
                + secretName
                ;

        //Repository Public Key 가져오기
        String publicKey = app.getRepoPublicKey();

        //encrypted_value :  LibSodium 라이브러리로 encrypt
        //Repository Public Key를 이용해서 retrieve 되어야 함
        SodiumUtils.encrypt();


        Mono<String> userResponseDtoMono =
                WebClient.create(url)
                        .put()
                        .header("Authorization", "Bearer " + gitHubToken)
                        .retrieve()
                        .bodyToMono(String.class);

        userResponseDtoMono.subscribe(response -> {
            log.info("[ GitHub Utils ] Action Secret 생성 성공 : " + response);
        });

//        return null;
    }



    /**
     * <h1>Get Repository Public Key</h1>
     * 참고 : <a href= "https://docs.github.com/ko/rest/actions/secrets?apiVersion=2022-11-28#get-a-repository-public-key">Link</a> <br>
     * @Method : GET
     * @url : /repos/{owner}/{repo}/actions/secrets/public-key
     */

    public static GitHubPublicKeyDto getRepositoryPublicKey(
            App app,
            String gitHubToken) {

        String url = "https://api.github.com/"
                + "repos" + "/"
                + app.getOwner() + "/"
                + app.getRepoName() + "/"
                + "actions" + "/"
                + "secrets" + "/"
                + "public-key"
                ;

        Mono<GitHubPublicKeyDto> tokenDtoMono =
                WebClient.create(url)
                        .get()
                        .header("Authorization", "Bearer " + gitHubToken)
                        .retrieve()
                        .bodyToMono(GitHubPublicKeyDto.class);

        tokenDtoMono.subscribe(gitHubPublicKeyDtoResponse -> {
            log.info("[ GitHub Utils ] Repository Public Key ID : " + gitHubPublicKeyDtoResponse.getKey_id());
            log.info("[ GitHub Utils ] Repository Public Key  : " + gitHubPublicKeyDtoResponse.getKey());
        });

        return tokenDtoMono.block();
    }
}

package com.ds.deploysurfingbackend.global.utils;

import com.ds.deploysurfingbackend.domain.app.domain.App;
import com.ds.deploysurfingbackend.domain.app.dto.GitHubPublicKeyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class GitHubUtils {


    public static List<String> test(String gitHubToken) {

        Mono<String> userResponseDtoMono =
                WebClient.create("https://api.github.com")
                        .get()
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + gitHubToken)
                        .retrieve()
                        .bodyToMono(String.class);

        userResponseDtoMono.subscribe(response -> {
            log.info("[ GitHub Utils ] Test 성공 : " + response);
        });

        return null;
    }


    /**
     * <h1>깃허브 Action Secret 만들기</h1>
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

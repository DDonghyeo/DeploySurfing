package com.ds.deploysurfingbackend.domain.github.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record CreateCommitRequestDto(
        //레포지토리 소유자
        String owner,
        //레포지토리 이름
        String repo,
        // 생성할 경로 + 파일 이름
        String path,
        // 커밋 메세지
        String message,
        //커밋자
        Committer committer,
        //내용 -> Base64 Encoded
        String content,
        //브랜치 명
        String branch,
        //Update 경우. The blob SHA of the file being replaced.
        String sha,
        //헤더. 'X-GitHub-Api-Version': '2022-11-28' 추가
        Map<String, String> headers

) {
    @Builder
    public record Committer(
            //커밋자 이름
            String name,
            //커밋자 이메일
            String email
    )  { }
}

package com.ds.deploysurfingbackend.domain.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GitHubPublicKeyDto {

    public String key_id;

    public String key;
}

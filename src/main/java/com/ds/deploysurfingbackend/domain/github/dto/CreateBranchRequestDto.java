package com.ds.deploysurfingbackend.domain.github.dto;

public record CreateBranchRequestDto(
        String ref,
        String sha
) {}

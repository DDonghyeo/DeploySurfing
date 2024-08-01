package com.ds.deploysurfingbackend.domain.aws.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AMI ( Amazon Machine Image )
 * - 각 리전에 고유한 AMI 값 보유
 */
@Getter
@RequiredArgsConstructor
public enum EC2AMI {

    AMAZON_LINUX_2_AMI("ami-01b15011585ebc739"),
    AMAZON_LINUX_2023_AMI("ami-07d95467596b97099");

    private final String value;


}

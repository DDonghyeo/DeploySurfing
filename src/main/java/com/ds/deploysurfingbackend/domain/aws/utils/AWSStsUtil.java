package com.ds.deploysurfingbackend.domain.aws.utils;

import com.ds.deploysurfingbackend.domain.aws.exception.AwsErrorCode;
import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.StsException;

@Slf4j
@Component
public class AWSStsUtil {

    private static final String SESSION_NAME = "session_surfing";

    //Role
    public static StaticCredentialsProvider assumeRole(String roleArn, String roleSessionName, String accessKey, String secAccessKey) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                accessKey, secAccessKey);
        try {
            StsClient stsClient = StsClient.builder()
                    .region(Region.AP_NORTHEAST_2) //Seoul Region
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .build();


            AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(roleSessionName)
                    .build();

            AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
            Credentials credentials = roleResponse.credentials();
            return StaticCredentialsProvider.create(AwsSessionCredentials.create(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken()
            ));


        } catch (StsException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new CustomException(AwsErrorCode.INVALID_TOKEN);
        }
    }

    public static StaticCredentialsProvider createStaticCredential(User user) {
        return assumeRole(user.getAwsRoleArn(), SESSION_NAME,
                user.getAwsAccessKey(), user.getAwsSecretKey());
    }

}


package com.ds.deploysurfingbackend.domain.aws.utils;

import lombok.extern.slf4j.Slf4j;
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
public class AWSStsUtil {

    // The AWS IAM Identity Center identity (user) who executes this method does not have permission to list buckets.
    // The identity is configured in the [default] profile.
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
            throw new RuntimeException();
        }
    }

}

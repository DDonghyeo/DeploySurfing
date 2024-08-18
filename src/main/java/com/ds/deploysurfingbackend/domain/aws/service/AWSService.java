package com.ds.deploysurfingbackend.domain.aws.service;

import com.ds.deploysurfingbackend.domain.aws.entity.EC2;
import com.ds.deploysurfingbackend.domain.aws.exception.AwsErrorCode;
import com.ds.deploysurfingbackend.domain.aws.repository.EC2Repository;
import com.ds.deploysurfingbackend.domain.aws.utils.AWSInstanceUtils;
import com.ds.deploysurfingbackend.domain.aws.utils.AWSStsUtil;
import com.ds.deploysurfingbackend.domain.user.auth.AuthUser;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.domain.user.exception.UserErrorCode;
import com.ds.deploysurfingbackend.domain.user.repository.UserRepository;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AWSService {

    private final EC2Repository ec2Repository;
    private final UserRepository userRepository;

    //새로운 EC2 생성
    public void createEC2(AuthUser authUser, String name) {

        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        StaticCredentialsProvider role = AWSStsUtil.createStaticCredential(user);

        AWSInstanceUtils.createFreeTierEC2(name, role);
    }

    //EC2 일시 중지
    public void pauseEC2(AuthUser authUser, String ec2Id) {

        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        StaticCredentialsProvider role = AWSStsUtil.createStaticCredential(user);

        EC2 ec2 = ec2Repository.findByEc2Id(ec2Id).orElseThrow(() -> new CustomException(AwsErrorCode.EC2_NOT_FOUND));

        AWSInstanceUtils.pauseEC2(role, ec2Id);
    }
    //EC2 종료 (삭제)

    public void terminateEC2(AuthUser authUser, String ec2Id) {

        User user = userRepository.findByEmail(authUser.getEmail()).orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        StaticCredentialsProvider role = AWSStsUtil.createStaticCredential(user);

        EC2 ec2 = ec2Repository.findByEc2Id(ec2Id).orElseThrow(() -> new CustomException(AwsErrorCode.EC2_NOT_FOUND));

        AWSInstanceUtils.terminateEC2(role, ec2Id, ec2.getAssociationId(), ec2.getSecurityGroupId(), ec2.getKeyName());
    }
}

package com.ds.deploysurfingbackend.domain.aws.service;

import com.ds.deploysurfingbackend.domain.aws.entity.EC2;
import com.ds.deploysurfingbackend.domain.aws.repository.EC2Repository;
import com.ds.deploysurfingbackend.domain.aws.utils.AWSInstanceUtils;
import com.ds.deploysurfingbackend.domain.aws.utils.AWSStsUtil;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AWSService {

    private static final String SESSION_NAME = "session_surfing";

    private final EC2Repository ec2Repository;

    //새로운 EC2 생성
    public void createEC2(String name) {

        //접속 세션 이름
        String sessionName = "session_surfing";

        //TODO : 유저 정보 가져와서 세션 생성
        User user = new User();
        StaticCredentialsProvider role = getStaticCredential(user);

        AWSInstanceUtils.createFreeTierEC2(name, role);

        log.info("[ AWS Service ] EC2 생성 완료");
    }

    //EC2 일시 중지
    public void pauseEC2(String ec2Id) {

        //TODO : 유저 정보 가져와서 세션 생성
        User user = new User();
        StaticCredentialsProvider role = getStaticCredential(user);
        AWSInstanceUtils.pauseEC2(role, ec2Id);

    }
    //EC2 종료 (삭제)

    public void terminateEC2(String ec2Id) {

        //TODO : 유저 정보 가져와서 세션 생성
        User user = new User();
        StaticCredentialsProvider role = getStaticCredential(user);
        EC2 ec2 = ec2Repository.findByEc2Id(ec2Id).orElseThrow(() -> new CustomException(CommonErrorCode.EC2_NOT_FOUND));
        AWSInstanceUtils.terminateEC2(role, ec2Id, ec2.getAssociationId(), ec2.getSecurityGroupId(), ec2.getKeyName());
    }



    private static StaticCredentialsProvider getStaticCredential(User user) {
        return AWSStsUtil.assumeRole("role", SESSION_NAME,
                user.getAwsAccessKey(), user.getAwsSecretKey());
    }
}

package com.ds.deploysurfingbackend.domain.aws.service;

import com.ds.deploysurfingbackend.domain.aws.utils.AWSInstanceUtils;
import com.ds.deploysurfingbackend.domain.aws.utils.AWSStsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Slf4j
@Service
public class AWSService {

    public void createEC2(String name) {

        String sessionName = "session_surfing";
        StaticCredentialsProvider role = AWSStsUtil.assumeRole("role", sessionName,
                "", "");
        AWSInstanceUtils.createFreeTierEC2(name, role);
    }
}

package com.ds.deploysurfingbackend.utils;

import com.ds.deploysurfingbackend.repository.AppJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GitHubUtilsTest {

    String token;

    @Autowired
    AppJpaRepository appJpaRepository;

    @BeforeEach
    void setUp() {
        token = "";
    }

    @Test
    public void utilTest() {
        String token = "";
        GitHubUtils.test(token);
    }

    @Test
    public void getRepositoryPublicKeyTest() {
//        appJpaRepository.findById();
//        GitHubUtils.getRepositoryPublicKey()
    }

}

package com.ds.deploysurfingbackend.domain.aws.repository;

import com.ds.deploysurfingbackend.domain.aws.entity.EC2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EC2Repository extends JpaRepository<EC2, String> {

    Optional<EC2> findByEc2Id(String ec2Id);
}

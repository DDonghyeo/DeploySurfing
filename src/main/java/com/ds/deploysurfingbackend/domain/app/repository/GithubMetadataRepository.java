package com.ds.deploysurfingbackend.domain.app.repository;

import com.ds.deploysurfingbackend.domain.app.entity.GithubMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubMetadataRepository extends JpaRepository<GithubMetaData, Long> {

    Optional<GithubMetaData> findByApp_Id(String id);
}

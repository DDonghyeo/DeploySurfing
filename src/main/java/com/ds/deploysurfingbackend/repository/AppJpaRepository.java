package com.ds.deploysurfingbackend.repository;

import com.ds.deploysurfingbackend.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppJpaRepository extends JpaRepository<App, String> {

    List<App> findAllByUserId(Long userId);
}

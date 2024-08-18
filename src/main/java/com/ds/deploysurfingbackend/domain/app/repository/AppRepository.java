package com.ds.deploysurfingbackend.domain.app.repository;

import com.ds.deploysurfingbackend.domain.app.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<App, String> {

    List<App> findAllByUserId(Long userId);
}

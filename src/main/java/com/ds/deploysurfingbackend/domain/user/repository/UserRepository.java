package com.ds.deploysurfingbackend.domain.user.repository;

import com.ds.deploysurfingbackend.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

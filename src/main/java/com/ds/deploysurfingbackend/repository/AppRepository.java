package com.ds.deploysurfingbackend.repository;

import com.ds.deploysurfingbackend.domain.App;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class AppRepository {

    private final AppJpaRepository appJpaRepository;

    public void save(App app) {
        try {
            appJpaRepository.save(app);
        } catch (IllegalArgumentException illegalArgumentException) {
            // in case the given entity is null.
            throw new CustomException(ErrorCode.INVALID_VALUE);
        } catch (EntityExistsException entityExistsException){
            //EntityManager.persist() 메서드 중 throw 되는 Exception
            //Entity 중복일 경우 throw
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }
    }

    public void deleteById(String appId) {
        try {
            //TODO : 사용자가 앱 삭제 권한이 있는지 검사
            appJpaRepository.deleteById(appId);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new CustomException(ErrorCode.INVALID_VALUE);
        } catch (NullPointerException nullPointerException) {
            throw new CustomException(ErrorCode.NO_CONTENT_FOUND);
        }

    }

     public App findById(String appId) {
         return appJpaRepository.findById(appId).orElseThrow(() -> {
             throw new CustomException(ErrorCode.INVALID_VALUE);
         }
         );
    }

    public List<App> findAllByUserId(Long userId) {
        try {
            return appJpaRepository.findAllByUserId(userId);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new CustomException(ErrorCode.INVALID_VALUE);
        }

    }
}

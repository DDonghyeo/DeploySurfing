package com.ds.deploysurfingbackend.global.event;

import com.ds.deploysurfingbackend.global.event.entity.OutBoxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutBoxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) //비관 배타락
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")}) //락 타임아웃 3s
    @Query("""
            SELECT e FROM OutBoxEvent e
            WHERE (e.status = :status OR
                  (e.status = 'FAILED' AND e.retryCount < :maxRetries)) AND
                  (e.lastProcessedAt IS NULL OR
                   e.lastProcessedAt < :timeout)
            ORDER BY e.createdAt ASC
            """)
    List<OutBoxEvent> findUnpublishedEventsWithLock(
            @Param("status") OutboxStatus status,
            @Param("maxRetries") int maxRetries,
            @Param("timeout") LocalDateTime timeout,
            Pageable pageable
    );

}

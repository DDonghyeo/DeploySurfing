package com.ds.deploysurfingbackend.global.event;

import com.ds.deploysurfingbackend.global.entity.BaseTimeEntity;
import com.ds.deploysurfingbackend.global.event.EventType;
import com.ds.deploysurfingbackend.global.event.OutboxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "outbox_events")
public class OutBoxEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String aggregateId;

    private EventType eventType;

    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    //재시도 횟수
    private int retryCount;

    //마지막 처리 시도 시간
    private LocalDateTime lastProcessedAt;

    @Column(nullable = true)
    private String exceptionMessage;

    public void setStatus(OutboxStatus status) {
        this.status = status;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}

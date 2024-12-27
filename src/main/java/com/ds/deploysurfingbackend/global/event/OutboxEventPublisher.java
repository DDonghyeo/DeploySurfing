package com.ds.deploysurfingbackend.global.event;

import com.ds.deploysurfingbackend.global.annotation.RedissonLock;
import com.ds.deploysurfingbackend.global.event.entity.OutBoxEvent;
import com.ds.deploysurfingbackend.global.event.events.EC2Events;
import com.ds.deploysurfingbackend.global.event.events.GithubEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class OutboxEventPublisher {

    private static final int BATCH_SIZE = 10;
    private static final int MAX_RETRY = 3;
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @RedissonLock(value = "outbox-event-processing")
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishEvents() {
        // 처리 타임아웃 시간
        LocalDateTime timeoutThreshold = LocalDateTime.now().minus(PROCESSING_TIMEOUT);

        log.info("[Outbox Event Publisher] Finding Unpublished Events ...");
        List<OutBoxEvent> events = outboxEventRepository.findUnpublishedEventsWithLock(
                OutboxStatus.CREATED,
                MAX_RETRY,
                timeoutThreshold,
                Pageable.ofSize(BATCH_SIZE)
        );

        for (OutBoxEvent event : events) {
            log.info("[Outbox Event Publisher] Publishing event --> {}", event.getEventId());
            try {
                DeploymentEvent deploymentEvent = objectMapper.readValue(
                        event.getPayload(),
                        getEventClass(event.getEventType())
                );

                eventPublisher.publishEvent(deploymentEvent);
                log.info("[Outbox Event Publisher] Publishing success --> {}", event.getEventId());
                event.setStatus(OutboxStatus.PUBLISHED);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                handlePublishingFailure(event, e);
            }
        }
    }

    private Class<? extends DeploymentEvent> getEventClass(EventType eventType) {
        return switch (eventType) {
            case CREATE_EC2 -> EC2Events.EC2CreationEvent.class;
            case CONFIG_SECRET -> GithubEvents.SecretConfigurationEvent.class;
            case CREATE_BRANCH -> GithubEvents.DeployBranchCreationEvent.class;
            case CREATE_FILE-> GithubEvents.CreateFileEvent.GitHubPayload.class;
            case CREATE_SCRIPT -> GithubEvents.CreateFileEvent.GitHubPayload.class;
        };
    }

    private void handlePublishingFailure(OutBoxEvent event, Exception e) {
        event.setStatus(OutboxStatus.FAILED);
        event.setExceptionMessage(e.getMessage());
    }
}

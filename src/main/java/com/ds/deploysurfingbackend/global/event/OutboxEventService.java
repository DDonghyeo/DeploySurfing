package com.ds.deploysurfingbackend.global.event;

import com.ds.deploysurfingbackend.global.event.entity.OutBoxEvent;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void saveEvent(DeploymentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event.getPayload());
            outboxEventRepository.save(OutBoxEvent.of(event, payload));
        } catch (JsonProcessingException e) {
            throw new CustomException(CommonErrorCode.SERVER_ERROR);
        }
    }
}

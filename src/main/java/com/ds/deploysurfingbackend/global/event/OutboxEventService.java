package com.ds.deploysurfingbackend.global.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    public void saveEvent()
}

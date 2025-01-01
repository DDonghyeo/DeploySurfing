package com.ds.deploysurfingbackend.domain.aws.service;

import org.springframework.transaction.event.TransactionalEventListener;

public class AwsEventListener {

    @TransactionalEventListener()
    public void handleEC2Event() {

    }
}

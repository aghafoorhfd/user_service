package com.pmo.userservice.application.integration.service.impl;

import com.pmo.userservice.application.integration.service.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Component class customer queue for communication with ActiveMQ.
 */
@Component
@RequiredArgsConstructor
@Qualifier("CustomerQueueJmsPublisher")
public class CustomerQueueJmsPublisher implements MessagePublisher {

    private final JmsTemplate jmsTemplate;
    @Value("${spring.activemq.queue.customer}")
    String queue;

    /**
     * {@inheritDoc}
     */
    public void sendMessage(final Object message) {
        jmsTemplate.convertAndSend(queue, message);
    }

}
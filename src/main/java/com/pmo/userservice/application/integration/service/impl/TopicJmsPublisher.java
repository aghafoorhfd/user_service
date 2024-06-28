package com.pmo.userservice.application.integration.service.impl;

import com.pmo.userservice.application.integration.service.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Component class for topic communication with ActiveMQ.
 */
@Component
@RequiredArgsConstructor
@Qualifier("TopicJmsPublisher")
public class TopicJmsPublisher implements MessagePublisher {

    private final JmsTemplate jmsTemplate;

    @Value("${spring.activemq.topic}")
    String topic;

    /**
     * {@inheritDoc}
     */
    public void sendMessage(final Object message) {
        jmsTemplate.convertAndSend(topic, message);
    }

}
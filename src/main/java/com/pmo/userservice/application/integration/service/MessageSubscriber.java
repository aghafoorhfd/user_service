package com.pmo.userservice.application.integration.service;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Interface for communication(subscription) with ActiveMQ.
 */
public interface MessageSubscriber {

  /**
   * Subscribe to Jms notifications
   *
   * @param message Jms message being received
   */
  void receiveMessage(final Message message) throws JMSException;
}

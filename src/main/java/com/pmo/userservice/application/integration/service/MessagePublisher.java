package com.pmo.userservice.application.integration.service;

/**
 * Interface for communication with ActiveMQ.
 */
public interface MessagePublisher {

  /**
   * Push's message object to configured topic
   *
   * @param message a message object can be Hashmap, list or serializable object.
   */
  void sendMessage(final Object message);
}

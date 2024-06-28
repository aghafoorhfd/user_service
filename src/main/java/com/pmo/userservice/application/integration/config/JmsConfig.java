package com.pmo.userservice.application.integration.config;

import java.util.List;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

/**
 * Configuration class for integration with ActiveMQ.
 */
@Configuration
@EnableJms
@Slf4j
public class JmsConfig {

  @Value("${spring.activemq.broker-url}")
  private String brokerUrl;

  @Value("${spring.activemq.user}")
  private String userName;

  @Value("${spring.activemq.password}")
  private String password;

  /**
   * Creates a bean of connection factory for activemq.
   *
   * @return bean of javax jms connection factory
   */
  @Bean
  public ConnectionFactory connectionFactory() {
    log.info("creating a bean of connectionFactory");
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    connectionFactory.setTrustedPackages(List.of("org.apache.activemq.command"));
    connectionFactory.setTrustAllPackages(false);
    connectionFactory.setUserName(userName);
    connectionFactory.setPassword(password);
    log.info("Successfully created a bean of connectionFactory");
    return connectionFactory;
  }

  /**
   * Creates a bean of caching connection factory for activemq.
   *
   * @return bean of jms caching connection factory
   */
  @Bean
  public CachingConnectionFactory cachingConnectionFactory() {
    log.info("creating a bean of connectionFactory");
    return new CachingConnectionFactory(connectionFactory());
  }

  /**
   * Creates a bean of jms template
   *
   * @return bean of jms template
   */
  @Bean
  public JmsTemplate jmsTemplate() {
    log.info("Creating a bean of JmsTemplate");
    JmsTemplate template = new JmsTemplate();
    template.setConnectionFactory(connectionFactory());
    template.setPubSubDomain(true);
    template.setDestinationResolver(destinationResolver());
    template.setDeliveryPersistent(true);
    log.info("Successfully created a bean of JmsTemplate");
    return template;
  }

  /**
   *
   * @param connectionFactory to create instance of default jms listener container factory
   * @param configures  the default jms lister container factory
   * @return
   */
  @Bean
  public JmsListenerContainerFactory<DefaultMessageListenerContainer> jmsListenerContainerFactory(
      ConnectionFactory connectionFactory,
      DefaultJmsListenerContainerFactoryConfigurer configures) {
    log.info("Creating a bean of JmsListenerContainerFactory");
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    configures.configure(factory, connectionFactory);
    log.info("Successfully created a bean of JmsListenerContainerFactory");
    return factory;
  }

  /**
   * Creates a bean of dynamic destination resolver
   *
   * @return bean of dynamic destination resolver
   */
  @Bean
  DynamicDestinationResolver destinationResolver() {
    return new DynamicDestinationResolver() {
      @Override
      public Destination resolveDestinationName(Session session, String destinationName,
          boolean pubSubDomain) throws JMSException {
        boolean domain = !destinationName.endsWith("Queue");
        return super.resolveDestinationName(session, destinationName, domain);
      }
    };
  }

}
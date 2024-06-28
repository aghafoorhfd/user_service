

package com.pmo.userservice.application.integration.service.impl;

import static com.pmo.userservice.application.integration.utils.Constants.COMPANY_NAME;
import static com.pmo.userservice.application.integration.utils.Constants.CONFLICT_NAME;
import static com.pmo.userservice.application.integration.utils.Constants.IMPACTED_MEMBER_IDS;
import static com.pmo.userservice.application.integration.utils.Constants.ZERO_INDEX;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_TAGGED_MEMBER_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_TAGGED_MEMBER_TEMPLATE;

import com.pmo.common.dto.EmailTemplateModelDTO;
import com.pmo.common.util.EmailUtils;
import com.pmo.common.util.PMOUtil;
import com.pmo.userservice.application.integration.enums.NotificationType;
import com.pmo.userservice.application.integration.service.MessagePublisher;
import com.pmo.userservice.application.integration.service.MessageSubscriber;
import com.pmo.userservice.application.integration.utils.Constants;
import com.pmo.userservice.application.service.ApplicationService;
import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import com.pmo.userservice.domain.multitenancy.util.TenantContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.jms.JMSException;
import javax.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Component class for topic communication(project) with ActiveMQ.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectQueueJmsSubscriber implements MessageSubscriber {

    private final ApplicationService applicationService;
    private final MessagePublisher emailQueueJmsPublisher;

    /**
     * {@inheritDoc}
     */
    @JmsListener(destination = "${spring.activemq.queue.project}")
    public void receiveMessage(final Message message) throws JMSException {
        if (message instanceof ActiveMQMapMessage) {
            Object projectNotification =
                ((ActiveMQMapMessage) message).getObject(
                    NotificationType.TAGGED_MEMBER_NOTIFICATION.name());
            if (PMOUtil.isNotNull(projectNotification)) {
                Map<String, List<String>> notificationMap = (HashMap<String, List<String>>) projectNotification;
                List<String> impactedMemberIds = notificationMap.get(IMPACTED_MEMBER_IDS);
                setCurrentTenant(notificationMap.get(COMPANY_NAME).get(ZERO_INDEX), Boolean.FALSE);
                List<String> emails = applicationService.getEmailsByUserIds(impactedMemberIds.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList()));

                EmailTemplateModelDTO model = EmailTemplateModelDTO.builder()
                    .conflictName(notificationMap.get(CONFLICT_NAME).get(ZERO_INDEX))
                    .build();

                emailQueueJmsPublisher.sendMessage(
                    EmailUtils.generateEmailNotificationObject(
                        emails.toArray(new String[0]),
                        null,
                        null,
                        MAIL_MESSAGE_TAGGED_MEMBER_SUBJECT,
                        MAIL_MESSAGE_TAGGED_MEMBER_TEMPLATE,
                        model
                    ));
            }
        } else {
            log.error(String.format(Constants.MESSAGE_TYPE_NOT_IDENTIFIED, message));
        }

    }

    private void setCurrentTenant(String companyName, Boolean isDatabaseCreationAllowed) {
        TenantContext.setTenantInfo(TenantInfo.builder()
            .companyName(companyName)
            .isDatabaseCreationAllowed(isDatabaseCreationAllowed)
            .build());
    }
}

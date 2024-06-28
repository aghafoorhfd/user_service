package com.pmo.userservice.application.integration.service.impl;

import static com.pmo.userservice.application.integration.utils.Constants.COMPANY_NAME;
import static com.pmo.userservice.application.integration.utils.Constants.JSON_PARSING_EXCEPTION_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SET_PASSWORD_VERIFY_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_UPDATE_B2B_PLAN_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_UPDATE_B2B_PLAN_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.SPACE_STRING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmo.common.dto.EmailTemplateModelDTO;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.util.EmailUtils;
import com.pmo.common.util.PMOUtil;
import com.pmo.common.util.StringUtils;
import com.pmo.userservice.infrastructure.utils.DateTimeUtil;
import com.pmo.userservice.application.dto.ResendEmailVerificationRequestDTO;
import com.pmo.userservice.application.dto.UpdateSubscriptionDetailsDTO;
import com.pmo.userservice.application.dto.UserRegisterRequestDTO;
import com.pmo.userservice.application.integration.dto.EmailContentDTO;
import com.pmo.userservice.application.integration.enums.NotificationType;
import com.pmo.userservice.application.integration.service.MessagePublisher;
import com.pmo.userservice.application.integration.service.MessageSubscriber;
import com.pmo.userservice.application.integration.utils.Constants;
import com.pmo.userservice.application.service.ApplicationService;
import com.pmo.userservice.domain.model.RolePrivileges;
import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import com.pmo.userservice.domain.multitenancy.util.TenantContext;
import com.pmo.userservice.infrastructure.enums.BusinessType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.jms.JMSException;
import javax.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Component class for topic communication(subscription) with ActiveMQ.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionQueueJmsSubscriber implements MessageSubscriber {

    private final ApplicationService applicationService;
    private final MessagePublisher emailQueueJmsPublisher;

    /**
     * {@inheritDoc}
     */
    @JmsListener(destination = "${spring.activemq.queue.subscription}")
    public void receiveMessage(final Message message) throws JMSException {
        if (message instanceof ActiveMQMapMessage) {
            Object subscriptionNotification =
                    ((ActiveMQMapMessage) message).getObject(
                            NotificationType.DEACTIVATE_SUBSCRIPTION_NOTIFICATION.name());
            if (subscriptionNotification != null) {
                Map<String, String> subscriptionMap = (HashMap<String, String>) subscriptionNotification;
                setCurrentTenant(subscriptionMap.get(COMPANY_NAME), Boolean.FALSE);
                applicationService.deactivateCompanySubscription(subscriptionMap.get(Constants.CUSTOMER_ID),
                        subscriptionMap.get(Constants.SUBSCRIPTION_ID));
                return;
            }

            Object updatesSubscriptionNotification = ((ActiveMQMapMessage) message).getObject(
                    NotificationType.
                            UPDATE_SUBSCRIPTION_NOTIFICATION.name());
            if (updatesSubscriptionNotification != null) {
                Map<String, String> subscriptionMap = (HashMap<String, String>) updatesSubscriptionNotification;
                UpdateSubscriptionDetailsDTO updateSubscriptionDetails = mapToUpdateSubscriptionDetailsDTO(subscriptionMap);
                setCurrentTenant(updateSubscriptionDetails.getCompanyName(), Boolean.FALSE);
                applicationService.updateSubscriptionDetails(updateSubscriptionDetails);
            }
            Object addB2BSubscriptionNotification = ((ActiveMQMapMessage) message).getObject(
                    NotificationType.ADD_B2B_SUBSCRIPTION_NOTIFICATION.name());
            if (PMOUtil.isNotNull(addB2BSubscriptionNotification)) {
                Map<String, String> subscriptionMap = (HashMap<String, String>) addB2BSubscriptionNotification;
                log.info("Reading subscription details of B2B customer having Id {}", subscriptionMap.get(Constants.CUSTOMER_ID));
                UserRegisterRequestDTO userRegisterRequest = UserRegisterRequestDTO.builder()
                        .firstName(subscriptionMap.get(Constants.FIRST_NAME))
                        .lastName(subscriptionMap.get(Constants.LAST_NAME))
                        .email(subscriptionMap.get(Constants.EMAIL))
                        .companyType(BusinessType.B2B.getCode())
                        .companyId(UUID.fromString(subscriptionMap.get(Constants.CUSTOMER_ID)))
                        .organizationName(subscriptionMap.get(Constants.COMPANY_NAME))
                        .build();

                UpdateSubscriptionDetailsDTO updateSubscriptionDetails = mapToUpdateSubscriptionDetailsDTO(subscriptionMap);
                setCurrentTenant(updateSubscriptionDetails.getCompanyName(), Boolean.TRUE);
                EmailContentDTO emailContentDTO = mapToEmailContentDTO(subscriptionMap);
                emailContentDTO.setTrialPeriod(Integer.parseInt(subscriptionMap.get(Constants.TRIAL_PERIOD)));
                applicationService.registerEnterpriseUser(userRegisterRequest, emailContentDTO, updateSubscriptionDetails);
            }

            Object updateB2BSubscriptionNotification = ((ActiveMQMapMessage) message).getObject(
                    NotificationType.UPDATE_B2B_SUBSCRIPTION_NOTIFICATION.name());
            if (PMOUtil.isNotNull(updateB2BSubscriptionNotification)) {
                Map<String, String> updateSubscriptionMap = (HashMap<String, String>) updateB2BSubscriptionNotification;
                setCurrentTenant(updateSubscriptionMap.get(COMPANY_NAME), Boolean.FALSE);

                applicationService.updateSubscriptionDetails(mapToUpdateSubscriptionDetailsDTO(updateSubscriptionMap));


                EmailTemplateModelDTO model = EmailTemplateModelDTO.builder()
                        .companyName(updateSubscriptionMap.get(COMPANY_NAME))
                        .planName(updateSubscriptionMap.get(Constants.PLAN_NAME))
                        .currencyCode(updateSubscriptionMap.get(Constants.CURRENCY_CODE))
                        .totalAmountPerCycle(new BigDecimal(updateSubscriptionMap.get(Constants.TOTAL_AMOUNT_PER_MONTH)))
                        .totalAmountToBeCharged(new BigDecimal(updateSubscriptionMap.get(Constants.TOTAL_AMOUNT_TO_BE_CHARGED)))
                        .endDate(updateSubscriptionMap.get(Constants.END_DATE))
                        .totalLicenses(Integer.decode(updateSubscriptionMap.get(Constants.TOTAL_LICENSES)))
                        .build();

                emailQueueJmsPublisher.sendMessage(
                        EmailUtils.generateEmailNotificationObject(
                                updateSubscriptionMap.get(Constants.EMAIL),
                                null,
                                null,
                                MAIL_MESSAGE_UPDATE_B2B_PLAN_SUBJECT,
                                MAIL_MESSAGE_UPDATE_B2B_PLAN_TEMPLATE,
                                model
                        ));
            }
            Object resendB2BSubscriptionVerificationInvite = ((ActiveMQMapMessage) message).getObject(
                NotificationType.RESEND_B2B_SUBSCRIPTION_VERIFICATION_INVITE.name());
            if (PMOUtil.isNotNull(resendB2BSubscriptionVerificationInvite)) {
                Map<String, String> customerInviteDetailsMap = (HashMap<String, String>) resendB2BSubscriptionVerificationInvite;
                ResendEmailVerificationRequestDTO resendEmailVerificationRequest =
                    generateResendEmailVerificationRequest(customerInviteDetailsMap);
                setCurrentTenant(resendEmailVerificationRequest.getCompanyName(), Boolean.FALSE);
                String verificationUrl = applicationService.generateResendInviteVerificationURL(resendEmailVerificationRequest.getEmail());
                if (StringUtils.hasText(verificationUrl)) {
                    log.info("Reading verification details of B2B customer having Id {}", resendEmailVerificationRequest.getCompanyId());
                    EmailTemplateModelDTO model = EmailTemplateModelDTO.builder()
                        .verificationUrl(verificationUrl)
                        .user(resendEmailVerificationRequest.getFirstName()
                            .concat(SPACE_STRING)
                            .concat(resendEmailVerificationRequest.getLastName()))
                        .build();
                    emailQueueJmsPublisher.sendMessage(
                        EmailUtils.generateEmailNotificationObject(
                            resendEmailVerificationRequest.getEmail(),
                            null,
                            null,
                            MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT,
                            MAIL_MESSAGE_SET_PASSWORD_VERIFY_TEMPLATE,
                            model
                        ));
                }
            }
            Object overdueSubscriptionNotification =
                    ((ActiveMQMapMessage) message).getObject(
                            NotificationType.REACTIVATE_OVERDUE_SUBSCRIPTION_NOTIFICATION.name());
            if (PMOUtil.isNotNull(overdueSubscriptionNotification)) {
                Map<String, String> subscriptionMap = (HashMap<String, String>) overdueSubscriptionNotification;
                setCurrentTenant(subscriptionMap.get(COMPANY_NAME), Boolean.FALSE);
                applicationService.reActivateOverdueSubscriptionUsers(UUID.fromString(subscriptionMap.get(Constants.CUSTOMER_ID)),
                        UUID.fromString(subscriptionMap.get(Constants.SUBSCRIPTION_ID)));
            }
        } else {
            log.error(String.format(Constants.MESSAGE_TYPE_NOT_IDENTIFIED, message));
        }

    }

    private ResendEmailVerificationRequestDTO generateResendEmailVerificationRequest(
        Map<String, String> customerVerificationDetailsMap) {
        return ResendEmailVerificationRequestDTO.builder()
            .companyId(customerVerificationDetailsMap.get(Constants.CUSTOMER_ID))
            .firstName(customerVerificationDetailsMap.get(Constants.FIRST_NAME))
            .lastName(customerVerificationDetailsMap.get(Constants.LAST_NAME))
            .email(customerVerificationDetailsMap.get(Constants.EMAIL))
            .companyName(customerVerificationDetailsMap.get(Constants.COMPANY_NAME))
            .build();
    }

    /**
     * It converts a map of subscription details to UpdateSubscriptionDetailsDTO
     *
     * @param updateSubscriptionMap map to update subscription
     * @return instance update subscription
     */
    private UpdateSubscriptionDetailsDTO mapToUpdateSubscriptionDetailsDTO(
            Map<String, String> updateSubscriptionMap) {
        return UpdateSubscriptionDetailsDTO.builder()
                .companyId(UUID.fromString(updateSubscriptionMap.get(Constants.CUSTOMER_ID)))
                .subscriptionId(UUID.fromString(updateSubscriptionMap.get(Constants.SUBSCRIPTION_ID)))
                .planId(UUID.fromString(updateSubscriptionMap.get(Constants.PLAN_ID)))
                .totalLicenses(Integer.decode(updateSubscriptionMap.get(Constants.TOTAL_LICENSES)))
                .companyType((updateSubscriptionMap.get(Constants.PLAN_TYPE)))
                .packageStartDate(LocalDate.parse(updateSubscriptionMap.get(Constants.START_DATE)))
                .packageEndDate(LocalDate.parse(updateSubscriptionMap.get(Constants.END_DATE)))
                .companyName(updateSubscriptionMap.get(COMPANY_NAME))
                .rolePrivilegesList(readAndMapToRolePrivileges(updateSubscriptionMap.get(Constants.PRIVILEGES)))
                .build();
    }

    /**
     * reads Json formatted ScreenRolePrivileges Object list and convert it into list of RolePrivileges
     *
     * @param rolePrivileges json formatted user screen privileges
     * @return {@link RolePrivileges}
     */
    private List<RolePrivileges> readAndMapToRolePrivileges(String rolePrivileges) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (rolePrivileges.isEmpty()) {
            return Collections.emptyList();
        }
        // We have to replace the attribute names according to the auditEntity in this service because the attributes
        // names are not sync in both of these services
        rolePrivileges =  rolePrivileges.replace("createdAt", "createdDate");
        rolePrivileges =  rolePrivileges.replace("updatedAt", "lastModifiedDate");
        rolePrivileges =  rolePrivileges.replace("updatedBy", "lastModifiedBy");

        try {
            //here we are reading Json value into List of RolePrivileges
            return objectMapper.readValue(rolePrivileges, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            throw new ApplicationException(JSON_PARSING_EXCEPTION_ERROR_MESSAGE);
        }
    }

    private void setCurrentTenant(String companyName, Boolean isDatabaseCreationAllowed) {
        TenantContext.setTenantInfo(TenantInfo.builder()
                .companyName(companyName)
                .isDatabaseCreationAllowed(isDatabaseCreationAllowed)
                .build());
    }

    private EmailContentDTO mapToEmailContentDTO(Map<String, String> subscriptionMap) {
        BigDecimal totalAmountPerCycle = new BigDecimal(subscriptionMap.get(Constants.TOTAL_AMOUNT_PER_MONTH));
        BigDecimal totalAmountTobeCharged = new BigDecimal(subscriptionMap.get(Constants.TOTAL_AMOUNT_TO_BE_CHARGED));

        return EmailContentDTO.builder()
                .currencyCode(subscriptionMap.get(Constants.CURRENCY_CODE))
                .totalAmountPerCycle(totalAmountPerCycle)
                .totalAmountTobeCharged(totalAmountTobeCharged)
                .currentCycleStartDate(DateTimeUtil.ConvertToEmailFormat(LocalDate.parse(subscriptionMap.get(Constants.CURRENT_CYCLE_START_DATE))))
                .currentCycleEndDate(DateTimeUtil.ConvertToEmailFormat(LocalDate.parse(subscriptionMap.get(Constants.CURRENT_CYCLE_END_DATE))))
                .build();

    }

}

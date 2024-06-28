package com.pmo.userservice.application.integration.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum carrying notification types for message broker.
 */
public enum NotificationType {
  EMAIL_NOTIFICATION,
  DEACTIVATE_SUBSCRIPTION_NOTIFICATION,
  UPDATE_SUBSCRIPTION_NOTIFICATION,
  ADD_CUSTOMER_NOTIFICATION,
  ADD_B2B_CUSTOMER_NOTIFICATION,
  ADD_B2B_SUBSCRIPTION_NOTIFICATION,
  RESEND_B2B_SUBSCRIPTION_VERIFICATION_INVITE,
  UPDATE_B2B_SUBSCRIPTION_NOTIFICATION,
  REACTIVATE_OVERDUE_SUBSCRIPTION_NOTIFICATION,
  TAGGED_MEMBER_NOTIFICATION;


    @Getter
  @AllArgsConstructor
  public enum Status {
    ACTIVE("ACTIVE", "Active"),
    IN_ACTIVE("IN_ACTIVE", "In Active"),
    DELETED("DELETED", "Deleted");

    private final String code;
    private final String title;
  }
}

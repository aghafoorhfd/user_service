package com.pmo.userservice.application.integration.utils;

/**
 * Application constants.
 */
public final class Constants {

    //Rename it to constant
    public static final String JSON_PARSING_EXCEPTION_ERROR_MESSAGE = "Error occurred while parsing JSON";
    public static final String CUSTOMER_ID = "customerId";
    public static final String COMPANY_NAME = "companyName";
    public static final String INVITE = "invite";
    public static final String CONFLICT_NAME = "conflictName";
    public static final int ZERO_INDEX = 0;
    public static final String IMPACTED_MEMBER_IDS = "impactedMemberIds";
    public static final String BEARER = "Bearer ";
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String PLAN_ID = "planId";
    public static final String TOTAL_LICENSES = "totalLicenses";
    public static final String PLAN_NAME = "planName";
    public static final String PLAN_TYPE = "planType";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String PRIVILEGES = "privileges";
    public static final String EMAIL = "email";
    public static final String GOLD_PLAN_ID = "goldPlanId";
    public static final String TOTAL_AMOUNT_TO_BE_CHARGED = "totalAmountToBeCharged";
    public static final String TOTAL_AMOUNT_PER_MONTH = "totalAmountPerMonth";
    // error messages
    public static final String MESSAGE_TYPE_NOT_IDENTIFIED = "Unable to identify message type. %s";
    public static final String PLAN_PACKAGE_ID = "planPackageId";
    public static final String REQUIRED_LICENSES = "requiredLicenses";
    public static final String PERIOD = "\\.";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String TRIAL_PERIOD = "trialPeriod";
    public static final String CURRENT_CYCLE_START_DATE = "currentCycleStartDate";
    public static final String CURRENT_CYCLE_END_DATE = "currentCycleEndDate";
    public static final String NO_SUBSCRIPTION_FOUND_ERROR_MESSAGE = "user does not have any " +
            "active subscription";
    public static final String PENDING_USER_ERROR_MESSAGE = "Dear user, please complete your sign up process";
    public static final String REJECTED_USER_ERROR_MESSAGE = "Dear user, your access is in rejected state, please contact admin";
    public static final String INACTIVE_USER_ERROR_MESSAGE = "Dear user, your licence is revoked";
    public static final String DECISION_PENDING_ERROR_MESSAGE = "Dear user, your license decision is still pending";

    public static final String MIN_LICENSE_MESSAGE = "licenses should be 10 or more";

    private Constants() {
    }
}

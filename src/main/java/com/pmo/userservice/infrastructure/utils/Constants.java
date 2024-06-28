package com.pmo.userservice.infrastructure.utils;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for validations
    public static final String LOGIN_REGEX = "^(?=.{2,30}@)(?=[a-zA-Z0-9.]*[a-zA-Z])[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)" +
            "*@[\\w-]{2,252}\\.[\\w-.]{1,6}$";
    public static final String PHONE_NUMBER_REGEX = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]{0,15}$";
    public static final String NAME_REGEX = "(?i)[a-z]{2,30}";
    public static final String SPACE_STRING = " ";
    public static final String COMPANY_NAME_REGEX = "^[a-z0-9]*$";
    //-------------------Application Email Messages Start--------------------//
    public static final String MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT = "Welcome to PMO Tracker";
    public static final String MAIL_MESSAGE_SET_PASSWORD_VERIFY_TEMPLATE = "verifyEmail";
    public static final String MAIL_MESSAGE_ADD_B2B_CUSTOMER_TEMPLATE = "b2bAddCustomer";

    public static final String MAIL_MESSAGE_RESET_PASSWORD_VERIFY_SUBJECT = "Reset Password";
    public static final String MAIL_MESSAGE_RESET_PASSWORD_VERIFY_TEMPLATE = "resetPassword";

    public static final String MAIL_MESSAGE_SUBSCRIPTION_ENDED_SUBJECT = "Subscription Ended";
    public static final String MAIL_MESSAGE_SUBSCRIPTION_ENDED_TEMPLATE = "endSubscription";

    public static final String MAIL_MESSAGE_USER_ACTIVATED_SUBJECT = "Account Reactivated";
    public static final String MAIL_MESSAGE_USER_REACTIVATED_TEMPLATE = "reactivateUser";

    public static final String MAIL_MESSAGE_PM_RM_INVITE_SUBJECT = "Invitation to PMO";
    public static final String MAIL_MESSAGE_PM_RM_INVITE_TEMPLATE = "inviteGeneralUser";


    public static final String MAIL_MESSAGE_UPDATE_B2B_PLAN_SUBJECT = "Updated invoice details for Plan";

    public static final String MAIL_MESSAGE_TAGGED_MEMBER_SUBJECT = "Member tagged in a risk";
    public static final String MAIL_MESSAGE_TAGGED_MEMBER_TEMPLATE = "memberTaggedInRisk";
    public static final String MAIL_MESSAGE_UPDATE_B2B_PLAN_TEMPLATE = "b2bUpgradePlan";

    public static final String MAIL_MESSAGE_USER_LICENSE_REVOKED_SUBJECT = "License Revoked";
    public static final String MAIL_MESSAGE_USER_LICENSE_REVOKED_TEMPLATE = "userLicenseRevoked";

    public static final String MAIL_MESSAGE_SUBSCRIPTION_REACTIVATED_TEMPLATE = "subscriptionReactivation";
    public static final String MAIL_MESSAGE_SUBSCRIPTION_REACTIVATED_SUBJECT = "Subscription Reactivated";

    //-------------------Application Email Messages End--------------------//

    // Error messages
    public static final String ALREADY_REGISTERED_ERROR_MESSAGE = "User with email '%s'";
    public static final String DUPLICATE_PHONE_ERROR_MESSAGE = "User with phone '%s'";
    public static final String ALREADY_REGISTERED_COMPANY_ERROR_MESSAGE = "Company already registered";
    public static final String NOT_REGISTERED_ERROR_MESSAGE = "User is not registered with id: '%s'.";
    public static final String USER = "User";
    public static final String INVALID_COMPANY_MESSAGE = "Should be a valid company";
    public static final String USER_DEACTIVATED_MESSAGE = "User is deactivated, Kindly contact support to setup your " +
            "account.";
    public static final String INVALID_USER = "should be a valid user";
    public static final String INVALID_REGISTRATION_STATUS_MESSAGE = "User status should be PENDING";
    public static final String USER_ALREADY_REJECTED_MESSAGE = "User has already rejected";
    public static final String EMAIL_NOT_VERIFIED_MESSAGE = "Email not verified by user: %s";
    public static final String INVALID_TOKEN_MESSAGE = "Invitation link is no longer valid, please sign up again";
    public static final String INVALID_TOKEN_CONTACT_ADMINISTRATOR_MESSAGE = "Invitation link is no longer valid, " +
            "please contact your administrator";
    public static final String INCOMPLETE_REGISTRATION_ERROR_MESSAGE = "User registration is not completed, kindly " +
            "complete sign up.";
    public static final String USER_NOT_AUTHORIZED_ERROR_MESSAGE = "User not authorized to create or edit this user";
    public static final String LICENSES_UTILIZED_ERROR_MESSAGE = "Maximum licenses available in subscription have " +
            "been utilized, kindly upgrade your subscription";
    public static final String INVALID_ACCESS_TYPE = "Invalid Access type";
    public static final String STATS_DATA_NOT_AVAILABLE = " No Data Available";
    public static final String RENEW_SUBSCRIPTION = "Maximum users limit reached, please upgrade your subscription";
    public static final String COMPANY_NOT_UPDATED = "Error occurred, unable to update company details";
    public static final String LICENSES_NOT_PRESENT = "Company have no active subscription";
    public static final String REVOKE_SUPER_ADMIN_MESSAGE = "Super admin access can not be revoked";
    public static final String DISABLED_USER_MESSAGE = "User is disabled, kindly contact admin";
    public static final String CARD_DETAILS_SCREEN_URL = "admin/paymentOptions";
    public static final String ADMIN_SCREEN_URL = "admin/";
    public static final String BILLING_SCREEN_URL = "admin/billing";
    public static final String PENDING_INVOICE_SCREEN_URL = "admin/pendingInvoice";
    public static final String COMPANY_OR_ROLE_NOT_FOUND_ERROR_MESSAGE = "Company or Role";
    public static final String JSON_PARSING_EXCEPTION_ERROR_MESSAGE = "Error occurred while parsing JSON";
    public static final String COMPANY_ID_OR_SUBSCRIPTION_ID_NOT_FOUND_ERROR_MESSAGE = "Company or Subscription";
    public static final String COMPANY = "company";
    public static final String SUPPORT = "Support";
    public static final String SUBDOMAIN = "sub domain";
    public static final String COMMA = ",";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String COMPANY_NAME = "companyName";
    public static final String ORGANIZATION_NAME = "organizationName";
    public static final String EMAIL = "email";
    public static final String ROLE = "role";
    public static final String USER_ID = "userId";
    public static final String IAM_ID = "iamId";
    public static final String COMPANY_ID = "companyId";
    public static final String X_TENANT_ID = "X-TENANT-ID";
    public static final String AUTHORIZATION = "Authorization";
    public static final String USER_CREDENTIALS_NOT_SET_ERROR_MESSAGE = "User credentials can not be set at the moment";
    public static final String SUBDOMAIN_ALREADY_EXIST = "Subdomain already exist";
    public static final String SUBDOMAIN_FORMAT_ERROR_MESSAGE = "Incorrect subdomain value, use only small letters and numbers";
    public static final String USER_UNABLE_LOGIN_ERROR_MESSAGE = "User unable to login at the moment";
    public static final String USER_NOT_DELETED_ERROR_MESSAGE = "User can not be deleted at the moment";
    public static final String USER_NOT_ENABLED_ERROR_MESSAGE = "User can not be enabled at the moment";
    public static final String USER_LIST_NOT_DELETED_ERROR_MESSAGE = "User list can not be deleted at the moment";
    public static final String USER_NOT_UPDATED_ERROR_MESSAGE = "User can not be updated at the moment";
    public static final String SUBSCRIPTION_ENDED_ERROR_MESSAGE = "Your subscription has ended, " +
            "please contact your super admin for further details";
    public static final String SESSION_EXPIRED_ERROR_MESSAGE = "Session has expired, please login again";

    public static final String PHYSICAL_NAMING_STRATEGY_VALUE = "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy";

    public static final String IMPLICIT_NAMING_STRATEGY_VALUE = "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy";

    public static final String US_DATE_PATTERN = "MM/dd/yyyy";
}

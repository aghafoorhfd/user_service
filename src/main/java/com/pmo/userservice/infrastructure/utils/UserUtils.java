package com.pmo.userservice.infrastructure.utils;

import com.pmo.userservice.application.integration.enums.NotificationType;
import com.pmo.userservice.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.pmo.userservice.application.integration.utils.Constants.PLAN_PACKAGE_ID;
import static com.pmo.userservice.application.integration.utils.Constants.REQUIRED_LICENSES;
import static com.pmo.userservice.application.integration.utils.Constants.CUSTOMER_ID;
import static com.pmo.userservice.application.integration.utils.Constants.INVITE;
import static com.pmo.userservice.infrastructure.utils.Constants.EMAIL;
import static com.pmo.userservice.infrastructure.utils.Constants.FIRST_NAME;
import static com.pmo.userservice.infrastructure.utils.Constants.LAST_NAME;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY_NAME;
import static com.pmo.userservice.infrastructure.utils.Constants.ORGANIZATION_NAME;


@Component
public class UserUtils {

    public Map<String, HashMap<String, String>> getCreateCustomerObject(User user) {
        HashMap<String, HashMap<String, String>> addCustomerMap = new HashMap<>();
        HashMap<String, String> customerMap = new HashMap<>();
        customerMap.put(CUSTOMER_ID, user.getCompany().getId().toString());
        customerMap.put(FIRST_NAME, user.getFirstName());
        customerMap.put(LAST_NAME, user.getLastName());
        customerMap.put(COMPANY_NAME, user.getCompany().getName());
        customerMap.put(ORGANIZATION_NAME, user.getCompany().getOrganizationName());
        customerMap.put(EMAIL, user.getEmail());
        customerMap.put(PLAN_PACKAGE_ID, user.getCompany().getPlanPackageId().toString());
        customerMap.put(REQUIRED_LICENSES, user.getCompany().getTotalLicenses().toString());
        addCustomerMap.put(NotificationType.ADD_CUSTOMER_NOTIFICATION.name(), customerMap);
        return addCustomerMap;
    }

    public Map<String, HashMap<String, String>> getCreateCustomerB2bObject(User user) {
        HashMap<String, HashMap<String, String>> addB2bCustomerMap = new HashMap<>();
        HashMap<String, String> customerMap = new HashMap<>();
        customerMap.put(CUSTOMER_ID, user.getCompany().getId().toString());
        customerMap.put(INVITE, user.getInvite().toString());
        addB2bCustomerMap.put(NotificationType.ADD_B2B_CUSTOMER_NOTIFICATION.name(), customerMap);
        return addB2bCustomerMap;
    }

    /**
     * Create verification link
     *
     * @param baseUrl frontend base path
     * @param path    frontend route path
     * @return String verification url to be sent to user
     */
    public static String generateVerificationUrl(String protocol, String companyName, String baseUrl, String path) {
        return protocol + companyName + baseUrl + path;
    }
}

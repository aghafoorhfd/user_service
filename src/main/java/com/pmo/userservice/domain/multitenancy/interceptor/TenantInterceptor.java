package com.pmo.userservice.domain.multitenancy.interceptor;

import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.exception.UnAuthorizedException;
import com.pmo.common.util.PMOUtil;
import com.pmo.common.util.StringUtils;
import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import com.pmo.userservice.domain.multitenancy.util.TenantContext;
import com.pmo.userservice.infrastructure.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.util.Map;
import java.util.Objects;

import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY_NAME_REGEX;
import static com.pmo.userservice.infrastructure.utils.Constants.SESSION_EXPIRED_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.SUBDOMAIN_FORMAT_ERROR_MESSAGE;
import static com.pmo.userservice.infrastructure.utils.Constants.X_TENANT_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements WebRequestInterceptor {

    @Value("${multi-tenancy.master.register-endpoint-url}")
    String registerEndpointUrl;

    /**
     * To Intercept the request and get tenant id from either token or request header,
     * so that we can make connection to the respective database.
     *
     * @param request the current web request
     */
    @Override
    public void preHandle(WebRequest request) {
        log.info("Configuring Tenant In Interceptor.");
        TenantInfo tenantInfo;
        Boolean isDatabaseCreationAllowed = Boolean.FALSE;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (PMOUtil.isNotNull(authentication) && authentication.getPrincipal() instanceof KeycloakPrincipal) {
            log.info("Fetching Company Name From Authentication Token...");
            KeycloakPrincipal<KeycloakSecurityContext> kp =
                    (KeycloakPrincipal<KeycloakSecurityContext>) authentication.getPrincipal();
            Map<String, Object> otherClaims = kp.getKeycloakSecurityContext().getToken().getOtherClaims();
            tenantInfo = TenantInfo.builder()
                    .isDatabaseCreationAllowed(false)
                    .companyName(String.valueOf(otherClaims.get(Constants.COMPANY_NAME)))
                    .build();
            log.info("Successfully Fetched Company Name {} From Authentication Token ", tenantInfo.getCompanyName());
        } else if (StringUtils.hasText(request.getHeader(X_TENANT_ID))) {
            log.info("Fetching Company Name From Header...");
            String companyName = validateCompanyName(request.getHeader(X_TENANT_ID));
            if (request instanceof ServletWebRequest) {
                String absoluteUrl = request.getDescription(true);
                String endpointUrl = absoluteUrl.split(";")[0];
                if (Objects.equals(registerEndpointUrl, endpointUrl)) {
                    isDatabaseCreationAllowed = Boolean.TRUE;
                }
            }
            tenantInfo = TenantInfo.builder()
                    .isDatabaseCreationAllowed(isDatabaseCreationAllowed)
                    .companyName(companyName)
                    .build();
            log.info("Successfully Fetched Company Name {} From Header", tenantInfo.getCompanyName());

        } else {
            throw new UnAuthorizedException(SESSION_EXPIRED_ERROR_MESSAGE);
        }
        TenantContext.setTenantInfo(tenantInfo);
        log.info("Successfully Configured Tenant In Interceptor.");
    }

    private String validateCompanyName(String companyName) {
        if(PMOUtil.isNotNull(companyName) && companyName.matches(COMPANY_NAME_REGEX))
            return companyName;
        else
            throw new ApplicationException(PmoErrors.BAD_REQUEST.getCode(), SUBDOMAIN_FORMAT_ERROR_MESSAGE);
    }

    @Override
    public void postHandle(@NonNull WebRequest request, ModelMap model) {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(@NonNull WebRequest request, Exception ex) {
        // NOOP
    }
}

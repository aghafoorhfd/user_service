package com.pmo.userservice.application.integration.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmo.common.util.PMOUtil;
import com.pmo.common.util.StringUtils;
import com.pmo.userservice.domain.multitenancy.domain.entity.Tenant;
import com.pmo.userservice.domain.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static com.pmo.userservice.application.integration.utils.Constants.PERIOD;
import static com.pmo.userservice.infrastructure.utils.Constants.AUTHORIZATION;
import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY_NAME;
import static com.pmo.userservice.infrastructure.utils.Constants.X_TENANT_ID;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.keycloak.representations.idm.CredentialRepresentation.SECRET;

@KeycloakConfiguration
@RequiredArgsConstructor
@Slf4j
public class KeycloakResolverConfig implements KeycloakConfigResolver {

    private final AdapterConfig masterConfig;
    private final TenantService tenantService;
    @Value("${kc.pmo-client}")
    private String pmoClient;
    @Value("${keycloak.auth-server-url}")
    private String serverUrl;
    @Value("${keycloak.ssl-required}")
    private String sslRequired;

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request request) {
        String companyName = null;
        String token = request.getHeader(AUTHORIZATION);
        if (PMOUtil.isNotNull(token)) {
            log.info("Parsing token to get company name");
            String[] jwtParts = token.split(PERIOD);
            String payload = new String(decodeBase64(jwtParts[1]));
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(payload);
                companyName = mapper.convertValue(node.get(COMPANY_NAME), String.class);

                log.info("Successfully parsed companyName: {} from token", companyName);
            } catch (JsonProcessingException e) {
                log.error("Error occurred while parsing JSON from token {}", e.getMessage());
            }
        } else if (StringUtils.hasText(request.getHeader(X_TENANT_ID))) {
            log.info("Parsing companyName from Request Header");
            companyName = StringUtils.convertToCompanyName(request.getHeader(X_TENANT_ID));
            log.info("Successfully parsed companyName: {} from Request Header", companyName);
        } else {
            return KeycloakDeploymentBuilder.build(masterConfig);
        }

        Tenant tenant = tenantService.getTenantByCompanyName(companyName);

        if (PMOUtil.isNull(tenant)) return KeycloakDeploymentBuilder.build(masterConfig);

        return this.buildKeycloakDeployment(companyName, tenant.getClientSecret(), tenant.getPublicKey());
    }

    private KeycloakDeployment buildKeycloakDeployment(String companyName, String secret, String publicKey) {

        AdapterConfig adapterConfig = new AdapterConfig();
        if (StringUtils.hasText(publicKey)) {
            adapterConfig.setRealmKey(publicKey);
        }
        adapterConfig.setRealm(companyName);
        adapterConfig.setRealmKey(publicKey);
        adapterConfig.setResource(pmoClient);
        adapterConfig.setAuthServerUrl(serverUrl);
        adapterConfig.setSslRequired(sslRequired);
        adapterConfig.setBearerOnly(true);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put(SECRET, secret);
        adapterConfig.setCredentials(credentials);

        return KeycloakDeploymentBuilder.build(adapterConfig);
    }
}

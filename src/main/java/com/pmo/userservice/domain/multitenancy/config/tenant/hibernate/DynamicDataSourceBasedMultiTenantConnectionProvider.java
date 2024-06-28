package com.pmo.userservice.domain.multitenancy.config.tenant.hibernate;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.pmo.common.exception.ApplicationException;
import com.pmo.common.util.PMOUtil;
import com.pmo.common.util.StringUtils;
import com.pmo.userservice.application.integration.client.CloudFlareClient;
import com.pmo.userservice.application.integration.dto.CreateSubdomainRequestDTO;
import com.pmo.userservice.domain.multitenancy.domain.entity.Tenant;
import com.pmo.userservice.domain.multitenancy.domain.entity.TenantInfo;
import com.pmo.userservice.domain.multitenancy.repository.TenantRepository;
import com.pmo.userservice.domain.multitenancy.util.TenantContext;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Component
public class DynamicDataSourceBasedMultiTenantConnectionProvider
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final long serialVersionUID = -460277105706399638L;

    private static final String TENANT_POOL_NAME_SUFFIX = "DataSource";

    @Qualifier("masterDataSource")
    private final transient DataSource masterDataSource;

    @Qualifier("masterDataSourceProperties")
    private final transient DataSourceProperties masterDataSourceProperties;

    @Autowired
    @Qualifier("tenantLiquibaseProperties")
    private transient LiquibaseProperties liquibaseProperties;

    private final transient TenantRepository tenantRepository;

    private final transient ResourceLoader resourceLoader;

    @Value("${multi-tenancy.tenant.datasource.url-prefix}")
    private String urlPrefix;

    @Value("${multi-tenancy.tenant.datasource.url-suffix}")
    private String urlSuffix;

    @Value("${multi-tenancy.master.datasource.username}")
    private String userName;

    @Value("${multi-tenancy.master.datasource.password}")
    private String password;

    @Value("${multi-tenancy.datasource-cache.maximumSize:100}")
    private Long maximumSize;

    @Value("${multi-tenancy.datasource-cache.expireAfterAccess:10}")
    private Integer expireAfterAccess;

    @Value("${cloudflare.access-token}")
    private String accessToken;

    @Value("${cloudflare.domain}")
    private String domain;

    @Value("${cloudflare.service.ttl}")
    private Integer ttl;

    @Value("${server.environment}")
    private String environment;

    private final CloudFlareClient cloudFlareClient;

    private transient LoadingCache<String, DataSource> tenantDataSources;

    @PostConstruct
    private void createCache() {
        tenantDataSources = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(Duration.ofMinutes(expireAfterAccess))
                .removalListener((RemovalListener<String, DataSource>) (tenantIdentifier, dataSource, removalCause) -> {
                    HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                    if (hikariDataSource != null) {
                        try {
                            releaseAnyConnection(hikariDataSource.getConnection());
                        } catch (SQLException e) {
                            log.error("Error releasing connection for tenant: {}", tenantIdentifier);
                        }
                        log.info("Closed datasource: {}", hikariDataSource.getPoolName());
                    }
                })
                .build(tenantIdentifier -> {
                        log.info("Fetching Tenant From Master, Tenant: {}", tenantIdentifier);
                        Optional<Tenant> tenant = tenantRepository.findByCompanyName(tenantIdentifier);
                        if (tenant.isPresent()) {
                            log.info("Successfully Fetched Tenant From Master, Tenant: {}", tenantIdentifier);
                            return createAndConfigureDataSource(tenant.get(), TenantContext.getTenantInfo().getIsDatabaseCreationAllowed());
                        } else if (Boolean.TRUE.equals(TenantContext.getTenantInfo().getIsDatabaseCreationAllowed())) {
                            log.info("Saving Tenant In Master, Tenant: {}", tenantIdentifier);
                            CreateSubdomainRequestDTO createSubdomainRequest;
                            createSubdomainRequest =
                                    CreateSubdomainRequestDTO.builder().type("A").name(appendEnvironment(tenantIdentifier))
                                            .proxied(false).ttl(ttl).content(domain).build();
                            cloudFlareClient.createSubDomain(accessToken, createSubdomainRequest);
                            Tenant newTenant = tenantRepository.save(Tenant.builder()
                                            .companyName(tenantIdentifier)
                                    .build());
                            log.info("Successfully Saved Tenant In Master, Tenant: {}", tenantIdentifier);
                            return createAndConfigureDataSource(newTenant, TenantContext.getTenantInfo().getIsDatabaseCreationAllowed());
                        }
                            throw new ApplicationException("Company not found");
                        }
                );
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return masterDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return tenantDataSources.get(tenantIdentifier);
    }

    @PreDestroy
    public void destroy() {
        tenantDataSources.asMap().values().forEach(dataSource -> {
            try {
                dataSource.getConnection().close();
            } catch (SQLException e) {
                log.error("Error closing datasource", e);
            }
        });
        log.info("Closed Connections Successfully !!");
    }

    private DataSource createAndConfigureDataSource(Tenant tenant, Boolean isDatabaseCreationAllowed) {
        log.info("Configuring datasource for tenant : {}", tenant.getCompanyName());
        String databaseUrl = urlPrefix + tenant.getCompanyName() + urlSuffix;

        HikariDataSource hikariDataSource = masterDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        hikariDataSource.setUsername(userName);
        hikariDataSource.setPassword(password);
        hikariDataSource.setJdbcUrl(databaseUrl);

        hikariDataSource.setPoolName(tenant.getId() + TENANT_POOL_NAME_SUFFIX);

        synchronized (this) {
            if (Boolean.TRUE.equals(isDatabaseCreationAllowed)) {
                try (Connection connection = hikariDataSource.getConnection()) {
                    DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
                    SpringLiquibase liquibase = this.getSpringLiquibase(tenantDataSource);
                    liquibase.afterPropertiesSet();
                } catch (SQLException | LiquibaseException e) {
                    log.error("Failed to run Liquibase for tenant " + tenant.getCompanyName(), e);
                }
                log.info("Liquibase ran for tenant " + tenant.getCompanyName());
            }
        }

        log.info("Configured datasource: {}", hikariDataSource.getPoolName());
        return hikariDataSource;
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(this.resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
        return liquibase;
    }

    private String appendEnvironment(String tenantIdentifier)  {
        String subdomain = "";
        //just checking null, because we accept empty values here
        if (PMOUtil.isNotNull(tenantIdentifier)) {
            subdomain = tenantIdentifier + "." + environment;
        }
        return subdomain;
    }
}

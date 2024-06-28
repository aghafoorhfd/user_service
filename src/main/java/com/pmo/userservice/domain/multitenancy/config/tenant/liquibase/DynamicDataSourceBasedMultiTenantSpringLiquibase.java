package com.pmo.userservice.domain.multitenancy.config.tenant.liquibase;

import com.pmo.userservice.domain.multitenancy.domain.entity.Tenant;
import com.pmo.userservice.domain.multitenancy.repository.TenantRepository;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Based on MultiTenantSpringLiquibase, this class provides Liquibase support for
 * multi-tenancy based on a dynamic collection of DataSources.
 */
@Getter
@Setter
@Slf4j
public class DynamicDataSourceBasedMultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    @Qualifier("tenantLiquibaseProperties")
    private LiquibaseProperties liquibaseProperties;

    @Value("${multi-tenancy.tenant.datasource.url-prefix}")
    private String urlPrefix;

    @Value("${multi-tenancy.tenant.datasource.url-suffix}")
    private String urlSuffix;

    @Value("${multi-tenancy.master.datasource.username}")
    private String userName;

    @Value("${multi-tenancy.master.datasource.password}")
    private String password;

    @Value("${multi-tenancy.tenant.datasource.drop-database-statement}")
    private String dropDatabaseStatement;

    private ResourceLoader resourceLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("DynamicDataSources based multi tenancy enabled");
        this.runOnAllTenants(tenantRepository.findAllByIsObsoleteFalse());
    }

    protected void runOnAllTenants(Collection<Tenant> tenants) {
        for(Tenant tenant : tenants) {
            if (tenant.isDelete()) {
                dropObsoleteDatabase(tenant);
                tenant.setObsolete(Boolean.TRUE);
            } else {
                createValidExistingDatabase(tenant);
            }
        }
        tenantRepository.saveAll(tenants);
    }

    private void createValidExistingDatabase(Tenant tenant) {

        log.info("Initializing Liquibase for tenant " + tenant.getCompanyName());
        String databaseUrl = urlPrefix + tenant.getCompanyName() + urlSuffix;

        try (Connection connection = DriverManager.getConnection(databaseUrl, userName, password)) {
            DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
            SpringLiquibase liquibase = this.getSpringLiquibase(tenantDataSource);
            liquibase.afterPropertiesSet();
        } catch (SQLException | LiquibaseException e) {
            log.error("Failed to run Liquibase for tenant " + tenant.getCompanyName(), e);
        }
        log.info("Liquibase ran for tenant " + tenant.getCompanyName());
    }

    private void dropObsoleteDatabase(Tenant tenantToDelete) {
        log.info("Dropping database for obsolete tenant " + tenantToDelete.getCompanyName());
        String databaseUrl = urlPrefix + tenantToDelete.getCompanyName();

        try (Connection connection = DriverManager.getConnection(databaseUrl, userName, password)) {
            // Drop the database only if it exists
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(dropDatabaseStatement + tenantToDelete.getCompanyName());
            }
        } catch (SQLException e) {
            log.error("Failed to drop database for obsolete tenant " + tenantToDelete.getCompanyName(), e);
        }
        log.info("drop statement ran for tenant " + tenantToDelete.getCompanyName());

    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(getResourceLoader());
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

}

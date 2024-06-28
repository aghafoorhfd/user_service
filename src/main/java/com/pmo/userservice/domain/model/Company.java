package com.pmo.userservice.domain.model;

import com.pmo.userservice.infrastructure.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Company extends AuditableEntity {

    @Id
    @Type(type = "uuid-char")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "organization_name", nullable = false, unique = true)
    private String organizationName;

    @Column(name = "company_type")
    @Enumerated(EnumType.STRING)
    private BusinessType companyType;

    @Column(name = "description")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_address_id")
    private Address address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_delete")
    private Boolean isDelete;

    @Column(name = "subscription_id")
    @Type(type = "uuid-char")
    private UUID subscriptionId;

    @Column(name = "plan_id")
    @Type(type = "uuid-char")
    private UUID planId;

    @Column(name = "total_licenses")
    private Integer totalLicenses;

    @Column(name = "used_licenses")
    private Integer usedLicenses;

    @Column(name = "package_start_date")
    private LocalDateTime packageStartDate;

    @Column(name = "package_end_date")
    private LocalDateTime packageEndDate;

    @Column(name = "is_subscription_active")
    private Boolean isSubscriptionActive;

    @Type(type = "uuid-char")
    @Column(name = "plan_package_id")
    private UUID planPackageId;
}

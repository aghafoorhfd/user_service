package com.pmo.userservice.domain.multitenancy.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenant")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Tenant {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    @Type(type = "uuid-char")
    private UUID id;
    @Column(name = "company_name", nullable = false)
    private String companyName;
    @Column(name = "client_secret")
    private String clientSecret;
    @Column(name = "public_key")
    private String publicKey;
    @Column(name = "is_obsolete")
    private boolean isObsolete;
    @Column(name = "is_delete")
    private boolean isDelete;

    public void setObsolete(boolean obsolete) {
        isObsolete = obsolete;
    }
}

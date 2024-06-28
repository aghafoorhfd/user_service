package com.pmo.userservice.domain.model;

import com.pmo.userservice.infrastructure.annotations.RoleValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "role_privileges")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RolePrivileges extends AuditableEntity implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Type(type = "uuid-char")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Type(type = "uuid-char")
    private UUID companyId;
    @RoleValidation
    private String role;
    @Column(name = "privileges", columnDefinition = "json")
    private String privileges;
    private boolean isDelete;
}

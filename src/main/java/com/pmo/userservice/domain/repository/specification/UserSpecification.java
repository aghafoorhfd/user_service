package com.pmo.userservice.domain.repository.specification;

import static com.pmo.userservice.infrastructure.utils.Constants.COMPANY;

import com.pmo.userservice.domain.model.Company;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.infrastructure.enums.FilterOperationEnum;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import com.pmo.userservice.infrastructure.filter.FilterCondition;
import com.pmo.userservice.infrastructure.filter.FilterUtils;
import java.util.List;
import java.util.UUID;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
@RequiredArgsConstructor
public class UserSpecification implements Specification<User> {

  private List<FilterCondition> filterConditions;
  private FilterUtils filterUtils;

  /**
   * {@inheritDoc}
   */
  @Override
  public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
    Predicate predicate = builder.conjunction();

    filterConditions.forEach(criteria -> {
      if (criteria.getOperator() == FilterOperationEnum.EQUAL) {
        if (criteria.getField().equals(COMPANY)) {
          Join<User, Company> customerJoin = root.join(COMPANY);
          predicate.getExpressions().add(builder.equal(customerJoin.get("id"),
              filterUtils.castToRequiredType(UUID.class, criteria.getValue().toString())));
        } else {
          predicate.getExpressions().add(builder.equal(
              root.get(criteria.getField()),
              filterUtils.castToRequiredType(root.get(criteria.getField()).getJavaType(),
                  criteria.getValue().toString())));
        }
      }
      if (criteria.getOperator() == FilterOperationEnum.IN) {
        predicate.getExpressions().add(root.get(criteria.getField())
            .in(filterUtils.castToRequiredType(RegistrationStatus.class,
                (List<String>) criteria.getValue())));
      }
    });
    return predicate;
  }
}

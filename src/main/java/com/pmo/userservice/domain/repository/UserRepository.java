package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.repository.projection.UserDetails;
import com.pmo.userservice.domain.repository.projection.UserDomain;
import com.pmo.userservice.domain.repository.projection.UserStats;
import com.pmo.userservice.infrastructure.enums.AccessType;
import com.pmo.userservice.infrastructure.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

  /**
   * Method finds a user by username.
   *
   * @param userName name of a user
   * @return Users info
   */
  Optional<User> findByEmail(String userName);

  /**
   * Retrieves a list of emails associated with the provided list of UUIDs.
   *
   * @param ids The list of UUIDs for which to retrieve emails.
   * @return A list of emails associated with the provided UUIDs.
   */
  @Query("SELECT u.email FROM User u WHERE u.id IN :ids")
  List<String> findEmailsByIdIn(@Param("ids") List<UUID> ids);

  /**
   * Method to find user categories by companyId.
   *
   * @param companyId company UUID
   * @return List of user domains
   */
  @Query(value =
      "SELECT access_type AS accessType, COUNT(*) AS noOfUsers FROM user WHERE company_id=:companyId "
          +
          "GROUP BY access_type",
      nativeQuery = true)
  List<UserDomain> findUserCategories(@Param("companyId") String companyId);

  /**
   * Method to find all users by companyId and access type
   *
   * @param companyId  company UUID
   * @param accessType access type of user
   * @return List of users
   */
  List<User> findAllByCompanyIdAndAccessTypeIn(UUID companyId, List<AccessType> accessType);

  /**
   * Method to find all user details by companyId and users ids
   *
   * @param companyId company UUID
   * @param ids       List of UUID of users
   * @return List of user details
   */
  List<UserDetails> findAllByCompanyIdAndIdIn(UUID companyId, List<UUID> ids);

  /**
   * Method to find all users by companyId, registration status and access type.
   *
   * @param companyId          company UUID
   * @param registrationStatus user registration status
   * @param accessType         access types of a user
   * @return List of users
   */
  List<User> findAllByCompanyIdAndStatusAndAccessTypeNot(UUID companyId,
      RegistrationStatus registrationStatus,
      AccessType accessType);

  /**
   * Method to find first user by companyId and address phone.
   *
   * @param companyId users company UUID
   * @param phone     user phone number
   * @return User info
   */
  Optional<User> findFirstByCompanyIdAndAddressPhone1(UUID companyId, String phone);
  @Query(value =
          "SELECT access_type AS accessType, COUNT(*) AS noOfUsers FROM user WHERE company_id = :companyId " +
                  "AND registration_status = 'pending' " +
                  "GROUP BY access_type",
          nativeQuery = true)
  List<UserDomain> findUserCountByRegistrationStatusPendingAndCompanyId(@Param("companyId") String companyId);
  @Query(value =
          "SELECT registration_status AS registrationStatus, COUNT(*) AS noOfUsers FROM user WHERE company_id = :companyId " +
                  "AND registration_status IN ('active', 'rejected', 'pending', 'REVOKED') " +
                  "GROUP BY registration_status",
          nativeQuery = true)
  List<UserStats> findUserCountForRegistrationStatusByCompanyId(@Param("companyId") String companyId);
}

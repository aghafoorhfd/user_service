package com.pmo.userservice.domain.repository;

import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.model.VerificationToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

  Optional<VerificationToken> findByUser(User user);

  Optional<VerificationToken> findByToken(UUID token);

  void deleteByUserId(UUID userId);
}

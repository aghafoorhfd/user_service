package com.pmo.userservice.domain.model.listener;

import com.pmo.userservice.infrastructure.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditorProvider implements AuditorAware<String> {
    private final TokenUtils tokenUtils;

    @Override
    public Optional<String> getCurrentAuditor() {
        String email;
        try {
            email = String.valueOf(tokenUtils.getTokenClaims().getEmail());
        } catch (Exception e) {
            //In case of user flows where login is not required.
            email = "PMO";
        }
        return Optional.of(email);
    }
}

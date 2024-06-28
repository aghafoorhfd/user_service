package com.pmo.userservice.domain.service;

import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.model.VerificationToken;

import java.util.Map;
import java.util.UUID;

/**
 * Interface for managing user verification tokens.
 */
public interface TokenService {

    /**
     * Create/Refresh verification token
     *
     * @param user a persistent object having user info
     * @return VerificationToken a persistent object having verification token info
     */
    VerificationToken generateVerificationToken(User user);

    /**
     * Generate verification token for new user and push verification link notification
     *
     * @param user a persistent object having user info
     * @return void no response
     */
    Map<String, String> getRegisterUserInviteEmailContent(User user);

    /**
     * @param verificationToken system generated token for email verification
     * @return VerificationToken a persistent object having verification token info
     */
    VerificationToken getVerificationToken(UUID verificationToken);

    /**
     * Generate verification token for existing user
     *
     * @param user a persistent object having user info
     * @return String content for email
     */
    Map<String, String> getForgotPasswordUserInviteEmailContent(User user);

}

package com.pmo.userservice.domain.service.impl;

import com.pmo.common.dto.EmailTemplateModelDTO;
import com.pmo.common.util.EmailUtils;
import com.pmo.userservice.domain.model.User;
import com.pmo.userservice.domain.model.VerificationToken;
import com.pmo.userservice.domain.repository.VerificationTokenRepository;
import com.pmo.userservice.domain.service.TokenService;
import com.pmo.userservice.infrastructure.utils.Constants;
import com.pmo.userservice.infrastructure.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.pmo.common.util.PMOUtil.validateAndGetObject;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_RESET_PASSWORD_VERIFY_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_RESET_PASSWORD_VERIFY_TEMPLATE;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT;
import static com.pmo.userservice.infrastructure.utils.Constants.MAIL_MESSAGE_SET_PASSWORD_VERIFY_TEMPLATE;

/**
 * Service class for managing user verification tokens.
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    @Value("${front-end.base-url}")
    private String frontEndBaseUrl;

    @Value("${front-end.protocol}")
    private String protocol;

    @Value("${front-end.path.reset-password}")
    private String frontEndResetPasswordPath;
    @Value("${front-end.path.set-password}")
    private String frontEndSetPasswordPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getRegisterUserInviteEmailContent(User user) {
        VerificationToken verificationToken = generateVerificationToken(user);
        String frontEndSetPasswordAbsolutePath = MessageFormat.format(frontEndSetPasswordPath,
                user.getCompany().getName(), verificationToken.getToken().toString());
        String verificationUrl = UserUtils.generateVerificationUrl(protocol, user.getCompany().getName(),
                frontEndBaseUrl, frontEndSetPasswordAbsolutePath);

        EmailTemplateModelDTO model = EmailTemplateModelDTO.builder()
                .verificationUrl(verificationUrl)
                .user(user.getFirstName())
                .build();

        return EmailUtils.generateEmailNotificationObject(
                user.getEmail(),
                null,
                null,
                MAIL_MESSAGE_SET_PASSWORD_VERIFY_SUBJECT,
                MAIL_MESSAGE_SET_PASSWORD_VERIFY_TEMPLATE,
                model
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VerificationToken generateVerificationToken(User user) {
        Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByUser(user);
        if (verificationTokenOptional.isPresent()) {
            VerificationToken verificationToken = verificationTokenOptional.get();
            verificationToken.setToken(UUID.randomUUID());
            verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            verificationToken = verificationTokenRepository.save(verificationToken);
            return verificationToken;
        } else {
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(UUID.randomUUID());
            verificationToken.setUser(user);
            verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            verificationToken = verificationTokenRepository.save(verificationToken);
            return verificationToken;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VerificationToken getVerificationToken(UUID token) {
        return validateAndGetObject(verificationTokenRepository.findByToken(token), Constants.INVALID_TOKEN_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getForgotPasswordUserInviteEmailContent(User user) {
        VerificationToken verificationToken = generateVerificationToken(user);
        String frontEndResetPasswordAbsolutePath = MessageFormat.format(frontEndResetPasswordPath,
                user.getCompany().getName(), verificationToken.getToken().toString());
        String resetUrl = UserUtils.generateVerificationUrl(protocol, user.getCompany().getName(),
                frontEndBaseUrl, frontEndResetPasswordAbsolutePath);

        EmailTemplateModelDTO model = EmailTemplateModelDTO.builder()
                .resetUrl(resetUrl)
                .user(user.getFirstName())
                .build();

        return EmailUtils.generateEmailNotificationObject(
                user.getEmail(),
                null,
                null,
                MAIL_MESSAGE_RESET_PASSWORD_VERIFY_SUBJECT,
                MAIL_MESSAGE_RESET_PASSWORD_VERIFY_TEMPLATE,
                model
        );
    }

}

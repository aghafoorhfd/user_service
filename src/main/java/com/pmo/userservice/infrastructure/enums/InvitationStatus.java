package com.pmo.userservice.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvitationStatus {

    SEND_INVITATION("SEND_INVITATION", "Send Invitation"),
    RESEND_INVITE("RESEND_INVITE", "Resend Invite"),
    INVITATION_SENT("INVITATION_SENT", "Invitation Sent"),
    INVITATION_VERIFIED("INVITATION_VERIFIED", "Invitation Verified"),
    INVITATION_EXPIRED("INVITATION_EXPIRED", "Invitation Expired");

    private final String code;
    private final String title;
}

package com.amortix.prime.service;

import org.springframework.security.core.Authentication;

public interface SecurityService {

    String getCurrentUsername();

    String getCurrentMemberCode();

    boolean isAdmin();

    boolean isStaff();

    boolean isMember();

    boolean hasRole(String role);

    /**
     * Validates that the authenticated user has permission to access the specified member's data.
     * ADMIN and STAFF can access any member.
     * MEMBER can only access their own record.
     *
     * @throws org.springframework.security.access.AccessDeniedException if unauthorized
     */
    void validateMemberAccess(String memberCode);

    /**
     * Validates that the authenticated user has permission to access the specified loan.
     *
     * @throws org.springframework.security.access.AccessDeniedException if unauthorized
     */
    void validateLoanAccess(String loanAccountNo);

    /**
     * Validates that the authenticated user has permission to access the specified deposit account.
     *
     * @throws org.springframework.security.access.AccessDeniedException if unauthorized
     */
    void validateDepositAccess(String depositAccountNo);
}

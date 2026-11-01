package com.amortix.prime.service.impl;

import com.amortix.prime.model.DepositAccount;
import com.amortix.prime.model.Loan;
import com.amortix.prime.model.UserAccount;
import com.amortix.prime.repository.DepositAccountRepository;
import com.amortix.prime.repository.LoanRepository;
import com.amortix.prime.repository.UserAccountRepository;
import com.amortix.prime.service.SecurityService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final UserAccountRepository userAccountRepository;
    private final LoanRepository loanRepository;
    private final DepositAccountRepository depositAccountRepository;

    public SecurityServiceImpl(UserAccountRepository userAccountRepository,
                               LoanRepository loanRepository,
                               DepositAccountRepository depositAccountRepository) {
        this.userAccountRepository = userAccountRepository;
        this.loanRepository = loanRepository;
        this.depositAccountRepository = depositAccountRepository;
    }

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public String getCurrentUsername() {
        Authentication auth = getAuth();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
    }

    @Override
    public String getCurrentMemberCode() {
        String username = getCurrentUsername();
        if (username == null) return null;
        return userAccountRepository.findByUsername(username)
                .map(UserAccount::getMemberCode)
                .orElse(null);
    }

    @Override
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    @Override
    public boolean isStaff() {
        return hasRole("ROLE_STAFF");
    }

    @Override
    public boolean isMember() {
        return hasRole("ROLE_MEMBER");
    }

    @Override
    public boolean hasRole(String role) {
        Authentication auth = getAuth();
        if (auth == null) return false;
        String checkRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equalsIgnoreCase(checkRole));
    }

    @Override
    public void validateMemberAccess(String memberCode) {
        if (isAdmin() || isStaff()) {
            return;
        }

        if (isMember()) {
            String myMemberCode = getCurrentMemberCode();
            if (myMemberCode != null && myMemberCode.equalsIgnoreCase(memberCode)) {
                return;
            }
            throw new AccessDeniedException("Data Isolation Violation: You are not authorized to access member record [" + memberCode + "]");
        }

        throw new AccessDeniedException("Access Denied: Unauthenticated or unrecognized role");
    }

    @Override
    public void validateLoanAccess(String loanAccountNo) {
        if (isAdmin() || isStaff()) {
            return;
        }

        if (isMember()) {
            String myMemberCode = getCurrentMemberCode();
            Optional<Loan> loan = loanRepository.findByLoanAccountNo(loanAccountNo);
            if (loan.isPresent() && myMemberCode != null && myMemberCode.equalsIgnoreCase(loan.get().getMemberCode())) {
                return;
            }
            throw new AccessDeniedException("Data Isolation Violation: You are not authorized to access loan [" + loanAccountNo + "]");
        }

        throw new AccessDeniedException("Access Denied: Unauthenticated or unrecognized role");
    }

    @Override
    public void validateDepositAccess(String depositAccountNo) {
        if (isAdmin() || isStaff()) {
            return;
        }

        if (isMember()) {
            String myMemberCode = getCurrentMemberCode();
            Optional<DepositAccount> deposit = depositAccountRepository.findByDepositAccountNo(depositAccountNo);
            if (deposit.isPresent() && myMemberCode != null && myMemberCode.equalsIgnoreCase(deposit.get().getMemberCode())) {
                return;
            }
            throw new AccessDeniedException("Data Isolation Violation: You are not authorized to access deposit account [" + depositAccountNo + "]");
        }

        throw new AccessDeniedException("Access Denied: Unauthenticated or unrecognized role");
    }
}

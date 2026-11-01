package com.amortix.prime.controller;

import com.amortix.prime.model.DailyAccountProcessing;
import com.amortix.prime.model.DepositAccount;
import com.amortix.prime.model.Loan;
import com.amortix.prime.model.Member;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class DashboardController {

    private final MemberRepository memberRepo;
    private final LoanRepository loanRepo;
    private final DepositAccountRepository depositRepo;
    private final AccountingService accountingService;
    private final MemberService memberService;
    private final LoanService loanService;
    private final DepositService depositService;
    private final SecurityService securityService;

    public DashboardController(MemberRepository memberRepo,
                               LoanRepository loanRepo,
                               DepositAccountRepository depositRepo,
                               AccountingService accountingService,
                               MemberService memberService,
                               LoanService loanService,
                               DepositService depositService,
                               SecurityService securityService) {
        this.memberRepo = memberRepo;
        this.loanRepo = loanRepo;
        this.depositRepo = depositRepo;
        this.accountingService = accountingService;
        this.memberService = memberService;
        this.loanService = loanService;
        this.depositService = depositService;
        this.securityService = securityService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        // If pure member (not admin/staff), show Member Self-Service Cockpit
        if (securityService.isMember() && !securityService.isAdmin() && !securityService.isStaff()) {
            String memberCode = securityService.getCurrentMemberCode();
            Member myMember = memberCode != null ? memberService.findByMemberCode(memberCode) : null;
            List<Loan> myLoans = memberCode != null ? loanService.findLoansByMemberCode(memberCode) : Collections.emptyList();
            List<DepositAccount> myDeposits = memberCode != null ? depositService.findByMemberCode(memberCode) : Collections.emptyList();

            BigDecimal totalLoanOutstanding = myLoans.stream()
                    .filter(l -> "ACTIVE".equalsIgnoreCase(l.getStatus()))
                    .map(Loan::getOutstandingPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDepositBalance = myDeposits.stream()
                    .filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()))
                    .map(DepositAccount::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("myMember", myMember);
            model.addAttribute("myLoans", myLoans);
            model.addAttribute("myDeposits", myDeposits);
            model.addAttribute("totalLoanOutstanding", totalLoanOutstanding);
            model.addAttribute("totalDepositBalance", totalDepositBalance);
            model.addAttribute("isMemberPortal", true);
            model.addAttribute("currentUser", auth != null ? auth.getName() : "Member");
            return "dashboard";
        }

        // Executive Cockpit for ADMIN & STAFF
        long activeMembers = memberRepo.countByStatus("ACTIVE");
        long activeLoans = loanRepo.countByStatus("ACTIVE");
        long pendingLoans = loanRepo.countByStatus("APPLIED");
        long activeDeposits = depositRepo.countByStatus("ACTIVE");

        DailyAccountProcessing dayBook = accountingService.getOrCreateDayBook(LocalDate.now());

        model.addAttribute("isMemberPortal", false);
        model.addAttribute("activeMembersCount", activeMembers);
        model.addAttribute("activeLoansCount", activeLoans);
        model.addAttribute("pendingLoansCount", pendingLoans);
        model.addAttribute("activeDepositsCount", activeDeposits);

        model.addAttribute("cashBalance", dayBook.getClosingCashBalance());
        model.addAttribute("bankBalance", dayBook.getClosingBankBalance());
        model.addAttribute("dayStatus", dayBook.getDayStatus());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("currentUser", auth != null ? auth.getName() : "Officer");

        return "dashboard";
    }
}

package com.amortix.prime.controller;

import com.amortix.prime.model.DailyAccountProcessing;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.AccountingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final MemberRepository memberRepo;
    private final LoanRepository loanRepo;
    private final DepositAccountRepository depositRepo;
    private final AccountingService accountingService;

    public DashboardController(MemberRepository memberRepo,
                               LoanRepository loanRepo,
                               DepositAccountRepository depositRepo,
                               AccountingService accountingService) {
        this.memberRepo = memberRepo;
        this.loanRepo = loanRepo;
        this.depositRepo = depositRepo;
        this.accountingService = accountingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        long activeMembers = memberRepo.count();
        long activeLoans = loanRepo.countByStatus("ACTIVE");
        long pendingLoans = loanRepo.countByStatus("APPLIED");
        long activeDeposits = depositRepo.countByStatus("ACTIVE");

        DailyAccountProcessing dayBook = accountingService.getOrCreateDayBook(LocalDate.now());

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

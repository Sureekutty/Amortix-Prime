package com.amortix.prime.controller;

import com.amortix.prime.model.DepositAccount;
import com.amortix.prime.model.DepositTransaction;
import com.amortix.prime.service.DepositService;
import com.amortix.prime.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/deposits")
public class DepositController {

    private final DepositService depositService;
    private final MemberService memberService;

    public DepositController(DepositService depositService, MemberService memberService) {
        this.depositService = depositService;
        this.memberService = memberService;
    }

    @GetMapping
    public String listDeposits(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        Page<DepositAccount> depositsPage = depositService.listDeposits(search, PageRequest.of(page, 10, Sort.by("depositDate").descending()));
        model.addAttribute("depositsPage", depositsPage);
        model.addAttribute("search", search);
        return "deposits/list";
    }

    @GetMapping("/open")
    public String openDepositForm(@RequestParam(value = "memberCode", required = false) String memberCode, Model model) {
        DepositAccount dep = new DepositAccount();
        dep.setDepositType("FXD");
        dep.setTenureMonths(12);
        dep.setPrincipalAmount(new BigDecimal("50000.00"));
        dep.setCompoundingFrequency("QUARTERLY");
        if (memberCode != null && !memberCode.isBlank()) {
            dep.setMemberCode(memberCode);
        }
        model.addAttribute("deposit", dep);
        model.addAttribute("activeMembers", memberService.listActiveMembers());
        return "deposits/open";
    }

    @PostMapping("/save")
    public String submitDeposit(@ModelAttribute DepositAccount deposit, Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            DepositAccount saved = depositService.openDeposit(deposit, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Deposit account successfully created! Account No: " + saved.getDepositAccountNo());
            return "redirect:/deposits/view/" + saved.getDepositAccountNo();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to open deposit: " + e.getMessage());
            return "redirect:/deposits/open";
        }
    }

    @GetMapping("/view/{depositAccountNo}")
    public String viewDeposit(@PathVariable String depositAccountNo, Model model) {
        DepositAccount dep = depositService.findByDepositAccountNo(depositAccountNo);
        List<DepositTransaction> txns = depositService.getDepositTransactions(depositAccountNo);
        model.addAttribute("deposit", dep);
        model.addAttribute("transactions", txns);
        return "deposits/view";
    }

    @PostMapping("/premature-close/{depositAccountNo}")
    public String prematureClose(@PathVariable String depositAccountNo,
                                 @RequestParam(value = "remarks", defaultValue = "Member requested closure") String remarks,
                                 Authentication auth,
                                 RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            DepositAccount closed = depositService.prematureCloseDeposit(depositAccountNo, remarks, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Deposit prematurely closed! Net payout: INR " + closed.getFinalPayoutAmount());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Closure failed: " + e.getMessage());
        }
        return "redirect:/deposits/view/" + depositAccountNo;
    }

    @PostMapping("/rcd-installment")
    public String recordRcdInstallment(@RequestParam("depositAccountNo") String depNo,
                                       @RequestParam("amount") BigDecimal amount,
                                       @RequestParam(value = "receiptNo", required = false) String receiptNo,
                                       Authentication auth,
                                       RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            depositService.recordRcdInstallment(depNo, amount, receiptNo, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Installment of INR " + amount + " recorded for " + depNo);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Payment failed: " + e.getMessage());
        }
        return "redirect:/deposits/view/" + depNo;
    }
}

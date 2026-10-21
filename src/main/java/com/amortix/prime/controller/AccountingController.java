package com.amortix.prime.controller;

import com.amortix.prime.model.DailyAccountProcessing;
import com.amortix.prime.model.Voucher;
import com.amortix.prime.service.AccountingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/accounting")
public class AccountingController {

    private final AccountingService accountingService;

    public AccountingController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @GetMapping("/daybook")
    public String viewDayBook(@RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        AccountingService.DayBookSummary summary = accountingService.generateDayBookSummary(queryDate);
        model.addAttribute("summary", summary);
        model.addAttribute("queryDate", queryDate);
        return "accounting/daybook";
    }

    @PostMapping("/daybook/close")
    public String closeDayBook(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               @RequestParam(value = "remarks", defaultValue = "Day end closing completed") String remarks,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            DailyAccountProcessing closed = accountingService.closeAndLockDayBook(date, remarks, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Day book for " + date + " has been closed and locked! Closing Cash: INR " + closed.getClosingCashBalance());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error closing day book: " + e.getMessage());
        }
        return "redirect:/accounting/daybook?date=" + date;
    }

    @GetMapping("/vouchers")
    public String listVouchers(@RequestParam(value = "type", required = false) String type,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        Page<Voucher> vouchersPage = accountingService.listVouchers(type, PageRequest.of(page, 15));
        model.addAttribute("vouchersPage", vouchersPage);
        model.addAttribute("currentType", type);
        return "accounting/vouchers";
    }

    @GetMapping("/voucher/new")
    public String newVoucherForm(Model model) {
        Voucher voucher = new Voucher();
        voucher.setVoucherType("PAYMENT");
        voucher.setPaymentMode("CASH");
        model.addAttribute("voucher", voucher);
        model.addAttribute("chartOfAccounts", accountingService.getChartOfAccounts());
        return "accounting/voucher_form";
    }

    @PostMapping("/voucher/save")
    public String saveVoucher(@ModelAttribute Voucher voucher, Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            Voucher saved = accountingService.createVoucher(voucher, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Voucher created successfully! Ref: " + saved.getVoucherNo());
            return "redirect:/accounting/vouchers";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to create voucher: " + e.getMessage());
            return "redirect:/accounting/voucher/new";
        }
    }

    @GetMapping("/trial-balance")
    public String trialBalance(Model model) {
        AccountingService.TrialBalanceReport report = accountingService.generateTrialBalance();
        model.addAttribute("report", report);
        return "accounting/trial_balance";
    }
}

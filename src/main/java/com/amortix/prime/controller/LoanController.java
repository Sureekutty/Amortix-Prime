package com.amortix.prime.controller;

import com.amortix.prime.model.Loan;
import com.amortix.prime.model.LoanTransaction;
import com.amortix.prime.model.Member;
import com.amortix.prime.model.SuretyReplacement;
import com.amortix.prime.repository.SuretyReplacementRepository;
import com.amortix.prime.service.LoanService;
import com.amortix.prime.service.MemberService;
import com.amortix.prime.service.PdfExportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final MemberService memberService;
    private final SuretyReplacementRepository suretyReplacementRepo;
    private final PdfExportService pdfExportService;

    public LoanController(LoanService loanService,
                          MemberService memberService,
                          SuretyReplacementRepository suretyReplacementRepo,
                          PdfExportService pdfExportService) {
        this.loanService = loanService;
        this.memberService = memberService;
        this.suretyReplacementRepo = suretyReplacementRepo;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping
    public String listLoans(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        Page<Loan> loansPage = loanService.listLoans(search, PageRequest.of(page, 10, Sort.by("applicationDate").descending()));
        model.addAttribute("loansPage", loansPage);
        model.addAttribute("search", search);
        return "loans/list";
    }

    @GetMapping("/apply")
    public String loanApplicationForm(@RequestParam(value = "memberCode", required = false) String memberCode, Model model) {
        Loan loan = new Loan();
        loan.setLoanType("LTL");
        loan.setTenureMonths(36);
        loan.setSanctionAmount(new BigDecimal("200000.00"));
        if (memberCode != null && !memberCode.isBlank()) {
            loan.setMemberCode(memberCode);
        }
        loan.getSureties().add(new Loan.LoanSurety("", "", "", BigDecimal.ZERO));
        loan.getSureties().add(new Loan.LoanSurety("", "", "", BigDecimal.ZERO));

        model.addAttribute("loan", loan);
        model.addAttribute("activeMembers", memberService.listActiveMembers());
        return "loans/apply";
    }

    @PostMapping("/save")
    public String submitLoanApplication(@ModelAttribute Loan loan, RedirectAttributes redirectAttrs) {
        try {
            Loan saved = loanService.applyForLoan(loan);
            redirectAttrs.addFlashAttribute("successMessage", "Loan application submitted successfully! Account No: " + saved.getLoanAccountNo());
            return "redirect:/loans/view/" + saved.getLoanAccountNo();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Loan application error: " + e.getMessage());
            return "redirect:/loans/apply";
        }
    }

    @GetMapping("/view/{loanAccountNo}")
    public String viewLoanDetails(@PathVariable String loanAccountNo, Model model) {
        Loan loan = loanService.findByLoanAccountNo(loanAccountNo);
        List<LoanTransaction> transactions = loanService.getLoanLedger(loanAccountNo);
        model.addAttribute("loan", loan);
        model.addAttribute("transactions", transactions);
        return "loans/view";
    }

    @PostMapping("/approve/{loanAccountNo}")
    public String approveLoan(@PathVariable String loanAccountNo, Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            loanService.approveLoan(loanAccountNo, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Loan " + loanAccountNo + " approved!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Approval failed: " + e.getMessage());
        }
        return "redirect:/loans/view/" + loanAccountNo;
    }

    @PostMapping("/disburse/{loanAccountNo}")
    public String disburseLoan(@PathVariable String loanAccountNo,
                               @RequestParam(value = "paymentMode", defaultValue = "BANK_TRANSFER") String mode,
                               @RequestParam(value = "chequeOrRefNo", defaultValue = "UTR-DIRECT") String refNo,
                               Authentication auth,
                               RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            loanService.sanctionAndDisburseLoan(loanAccountNo, mode, refNo, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Loan " + loanAccountNo + " successfully disbursed! Vouchers and ledgers updated.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Disbursement failed: " + e.getMessage());
        }
        return "redirect:/loans/view/" + loanAccountNo;
    }

    @GetMapping("/repay/{loanAccountNo}")
    public String repaymentForm(@PathVariable String loanAccountNo, Model model) {
        Loan loan = loanService.findByLoanAccountNo(loanAccountNo);
        model.addAttribute("loan", loan);
        return "loans/repay";
    }

    @PostMapping("/repay/{loanAccountNo}")
    public String submitRepayment(@PathVariable String loanAccountNo,
                                  @RequestParam("principalAmount") BigDecimal principal,
                                  @RequestParam("interestAmount") BigDecimal interest,
                                  @RequestParam(value = "paymentMode", defaultValue = "CASH") String mode,
                                  @RequestParam(value = "receiptNo", required = false) String receiptNo,
                                  @RequestParam(value = "remarks", defaultValue = "Ad-hoc repayment") String remarks,
                                  Authentication auth,
                                  RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            loanService.recordLoanRepayment(loanAccountNo, principal, interest, mode, receiptNo, remarks, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Repayment of INR " + principal.add(interest) + " recorded successfully!");
            return "redirect:/loans/view/" + loanAccountNo;
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Repayment failed: " + e.getMessage());
            return "redirect:/loans/repay/" + loanAccountNo;
        }
    }

    @GetMapping("/surety-replace")
    public String listSuretyReplacements(Model model) {
        List<SuretyReplacement> pending = suretyReplacementRepo.findByStatus("PENDING");
        List<SuretyReplacement> all = suretyReplacementRepo.findAll();
        model.addAttribute("pendingReplacements", pending);
        model.addAttribute("allReplacements", all);
        model.addAttribute("activeMembers", memberService.listActiveMembers());
        return "loans/surety_replace";
    }

    @PostMapping("/surety-replace/request")
    public String requestSuretyReplacement(@RequestParam("loanAccountNo") String loanNo,
                                           @RequestParam("oldSuretyMemberCode") String oldSurety,
                                           @RequestParam("newSuretyMemberCode") String newSurety,
                                           @RequestParam("reason") String reason,
                                           RedirectAttributes redirectAttrs) {
        try {
            loanService.requestSuretyReplacement(loanNo, oldSurety, newSurety, reason);
            redirectAttrs.addFlashAttribute("successMessage", "Surety replacement request submitted for approval.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Request failed: " + e.getMessage());
        }
        return "redirect:/loans/surety-replace";
    }

    @PostMapping("/surety-replace/approve/{id}")
    public String approveSuretyReplacement(@PathVariable String id, Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            loanService.approveSuretyReplacement(id, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Surety replacement approved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Approval failed: " + e.getMessage());
        }
        return "redirect:/loans/surety-replace";
    }

    @GetMapping("/pdf/{loanAccountNo}")
    public ResponseEntity<byte[]> downloadLoanSanctionPdf(@PathVariable String loanAccountNo) {
        Loan loan = loanService.findByLoanAccountNo(loanAccountNo);
        Member member = memberService.findByMemberCode(loan.getMemberCode());
        byte[] pdfBytes = pdfExportService.generateLoanSanctionPdf(loan, member);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Loan_Sanction_" + loanAccountNo + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}

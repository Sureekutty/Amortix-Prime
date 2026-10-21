package com.amortix.prime.controller;

import com.amortix.prime.model.DepositAccount;
import com.amortix.prime.model.Loan;
import com.amortix.prime.model.Member;
import com.amortix.prime.model.ThriftTransaction;
import com.amortix.prime.service.DepositService;
import com.amortix.prime.service.LoanService;
import com.amortix.prime.service.MemberService;
import com.amortix.prime.service.ThriftService;
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
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;
    private final DepositService depositService;
    private final ThriftService thriftService;

    public MemberController(MemberService memberService,
                            LoanService loanService,
                            DepositService depositService,
                            ThriftService thriftService) {
        this.memberService = memberService;
        this.loanService = loanService;
        this.depositService = depositService;
        this.thriftService = thriftService;
    }

    @GetMapping
    public String listMembers(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              Model model) {
        Page<Member> membersPage = memberService.listMembers(search, PageRequest.of(page, 10, Sort.by("memberCode").ascending()));
        model.addAttribute("membersPage", membersPage);
        model.addAttribute("search", search);
        return "members/list";
    }

    @GetMapping("/new")
    public String newMemberForm(Model model) {
        Member member = new Member();
        member.getAddresses().add(new Member.Address("PRESENT", "", "", "", "", ""));
        member.getNominees().add(new Member.Nominee("", "", 18, new BigDecimal("100.00")));
        model.addAttribute("member", member);
        return "members/form";
    }

    @PostMapping("/save")
    public String saveMember(@ModelAttribute Member member, RedirectAttributes redirectAttrs) {
        try {
            Member saved = memberService.registerMember(member);
            redirectAttrs.addFlashAttribute("successMessage", "Member successfully saved with code: " + saved.getMemberCode());
            return "redirect:/members/view/" + saved.getMemberCode();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error saving member: " + e.getMessage());
            return "redirect:/members/new";
        }
    }

    @GetMapping("/view/{memberCode}")
    public String viewMemberProfile(@PathVariable String memberCode, Model model) {
        Member member = memberService.findByMemberCode(memberCode);
        List<Loan> loans = loanService.findLoansByMemberCode(memberCode);
        List<DepositAccount> deposits = depositService.findByMemberCode(memberCode);
        List<ThriftTransaction> thriftHistory = thriftService.getMemberThriftLedger(memberCode);

        model.addAttribute("member", member);
        model.addAttribute("loans", loans);
        model.addAttribute("deposits", deposits);
        model.addAttribute("thriftHistory", thriftHistory);
        return "members/view";
    }

    @GetMapping("/settle/{memberCode}")
    public String memberSettlementView(@PathVariable String memberCode, Model model) {
        Member member = memberService.findByMemberCode(memberCode);
        List<Loan> loans = loanService.findLoansByMemberCode(memberCode);
        List<DepositAccount> deposits = depositService.findByMemberCode(memberCode);

        BigDecimal outstandingLoans = loans.stream()
                .filter(l -> "ACTIVE".equalsIgnoreCase(l.getStatus()))
                .map(l -> l.getOutstandingPrincipal().add(l.getOutstandingInterest()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal activeDeposits = deposits.stream()
                .filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()))
                .map(d -> d.getPrincipalAmount().add(d.getAccumulatedInterest()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossPayable = member.getShareCapitalBalance()
                .add(member.getThriftBalance())
                .add(activeDeposits);

        BigDecimal netPayable = grossPayable.subtract(outstandingLoans);

        model.addAttribute("member", member);
        model.addAttribute("shareRefund", member.getShareCapitalBalance());
        model.addAttribute("thriftRefund", member.getThriftBalance());
        model.addAttribute("depositRefund", activeDeposits);
        model.addAttribute("loanDeduction", outstandingLoans);
        model.addAttribute("netSettlementPayable", netPayable);

        return "members/settlement";
    }

    @PostMapping("/settle/{memberCode}/execute")
    public String executeMemberSettlement(@PathVariable String memberCode,
                                         @RequestParam(value = "remarks", defaultValue = "Full & Final Settlement") String remarks,
                                         Authentication auth,
                                         RedirectAttributes redirectAttrs) {
        try {
            String operator = auth != null ? auth.getName() : "ADMIN";
            MemberService.MemberSettlementResult res = memberService.calculateAndExecuteSettlement(memberCode, remarks, operator);
            redirectAttrs.addFlashAttribute("successMessage", "Full and final settlement executed! Net payout voucher created for INR " + res.netPayableAmount());
            return "redirect:/members/view/" + memberCode;
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Settlement failed: " + e.getMessage());
            return "redirect:/members/settle/" + memberCode;
        }
    }
}

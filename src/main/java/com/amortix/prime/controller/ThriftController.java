package com.amortix.prime.controller;

import com.amortix.prime.model.Member;
import com.amortix.prime.model.ThriftTransaction;
import com.amortix.prime.service.MemberService;
import com.amortix.prime.service.ThriftService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/thrift")
public class ThriftController {

    private final ThriftService thriftService;
    private final MemberService memberService;

    public ThriftController(ThriftService thriftService, MemberService memberService) {
        this.thriftService = thriftService;
        this.memberService = memberService;
    }

    @GetMapping
    public String thriftOverview(Model model) {
        List<Member> members = memberService.listActiveMembers();
        BigDecimal totalThriftPool = members.stream()
                .map(Member::getThriftBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyExpectedSubscription = members.stream()
                .map(Member::getMonthlyThriftAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("members", members);
        model.addAttribute("totalThriftPool", totalThriftPool);
        model.addAttribute("monthlyExpectedSubscription", monthlyExpectedSubscription);
        return "thrift/overview";
    }

    @PostMapping("/annual-interest")
    public String runAnnualThriftInterest(@RequestParam(value = "rate", defaultValue = "6.0") double rate,
                                          Authentication auth,
                                          RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            ThriftService.ThriftInterestRunResult res = thriftService.calculateAndCreditAnnualThriftInterest(rate, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Annual thrift interest run executed! Credited INR "
                    + res.totalInterestDistributed() + " across " + res.totalMembersProcessed() + " members.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Interest calculation error: " + e.getMessage());
        }
        return "redirect:/thrift";
    }

    @GetMapping("/member/{memberCode}")
    public String memberThriftLedger(@PathVariable String memberCode, Model model) {
        Member member = memberService.findByMemberCode(memberCode);
        List<ThriftTransaction> txns = thriftService.getMemberThriftLedger(memberCode);
        model.addAttribute("member", member);
        model.addAttribute("transactions", txns);
        return "thrift/member_ledger";
    }
}

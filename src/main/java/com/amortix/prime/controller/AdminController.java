package com.amortix.prime.controller;

import com.amortix.prime.model.AuditLog;
import com.amortix.prime.model.InterestRateRule;
import com.amortix.prime.model.Poll;
import com.amortix.prime.model.SocietyRule;
import com.amortix.prime.repository.AuditLogRepository;
import com.amortix.prime.repository.InterestRateRuleRepository;
import com.amortix.prime.repository.PollRepository;
import com.amortix.prime.repository.SocietyRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SocietyRuleRepository ruleRepo;
    private final InterestRateRuleRepository interestRepo;
    private final PollRepository pollRepo;
    private final AuditLogRepository auditRepo;

    public AdminController(SocietyRuleRepository ruleRepo,
                           InterestRateRuleRepository interestRepo,
                           PollRepository pollRepo,
                           AuditLogRepository auditRepo) {
        this.ruleRepo = ruleRepo;
        this.interestRepo = interestRepo;
        this.pollRepo = pollRepo;
        this.auditRepo = auditRepo;
    }

    @GetMapping("/rules")
    public String listRules(Model model) {
        List<SocietyRule> rules = ruleRepo.findAll();
        model.addAttribute("rules", rules);
        return "admin/rules";
    }

    @PostMapping("/rules/update")
    public String updateRule(@RequestParam("ruleCode") String ruleCode,
                             @RequestParam("ruleValue") String ruleValue,
                             RedirectAttributes redirectAttrs) {
        ruleRepo.findByRuleCode(ruleCode).ifPresent(r -> {
            r.setRuleValue(ruleValue);
            ruleRepo.save(r);
        });
        redirectAttrs.addFlashAttribute("successMessage", "Rule " + ruleCode + " updated to " + ruleValue);
        return "redirect:/admin/rules";
    }

    @GetMapping("/interest-rates")
    public String listInterestRates(Model model) {
        List<InterestRateRule> rates = interestRepo.findAll();
        model.addAttribute("rates", rates);
        return "admin/interest_rates";
    }

    @PostMapping("/interest-rates/update")
    public String updateInterestRate(@RequestParam("id") String id,
                                     @RequestParam("interestRate") Double rate,
                                     RedirectAttributes redirectAttrs) {
        interestRepo.findById(id).ifPresent(r -> {
            r.setInterestRate(rate);
            interestRepo.save(r);
        });
        redirectAttrs.addFlashAttribute("successMessage", "Interest rate updated successfully.");
        return "redirect:/admin/interest-rates";
    }

    @GetMapping("/polls")
    public String listPolls(Model model) {
        List<Poll> polls = pollRepo.findAll();
        model.addAttribute("polls", polls);
        return "admin/polls";
    }

    @PostMapping("/polls/vote")
    public String votePoll(@RequestParam("pollId") String pollId,
                           @RequestParam("option") String option,
                           Authentication auth,
                           RedirectAttributes redirectAttrs) {
        String user = auth != null ? auth.getName() : "ANONYMOUS";
        pollRepo.findById(pollId).ifPresent(p -> {
            if (!p.getMemberVotes().containsKey(user)) {
                p.getMemberVotes().put(user, option);
                p.getVoteCounts().put(option, p.getVoteCounts().getOrDefault(option, 0) + 1);
                pollRepo.save(p);
            }
        });
        redirectAttrs.addFlashAttribute("successMessage", "Your ballot has been cast!");
        return "redirect:/admin/polls";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<AuditLog> auditPage = auditRepo.findAllByOrderByTimestampDesc(PageRequest.of(page, 20));
        model.addAttribute("auditPage", auditPage);
        return "admin/audit_logs";
    }
}

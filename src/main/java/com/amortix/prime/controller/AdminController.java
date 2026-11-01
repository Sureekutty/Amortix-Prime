package com.amortix.prime.controller;

import com.amortix.prime.model.AuditLog;
import com.amortix.prime.model.InterestRateRule;
import com.amortix.prime.model.SocietyRule;
import com.amortix.prime.model.UserAccount;
import com.amortix.prime.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SocietyRuleRepository ruleRepo;
    private final InterestRateRuleRepository interestRepo;
    private final AuditLogRepository auditRepo;
    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminController(SocietyRuleRepository ruleRepo,
                           InterestRateRuleRepository interestRepo,
                           AuditLogRepository auditRepo,
                           UserAccountRepository userRepo,
                           PasswordEncoder passwordEncoder) {
        this.ruleRepo = ruleRepo;
        this.interestRepo = interestRepo;
        this.auditRepo = auditRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
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
                             Authentication auth,
                             RedirectAttributes redirectAttrs) {
        ruleRepo.findByRuleCode(ruleCode).ifPresent(r -> {
            r.setRuleValue(ruleValue);
            ruleRepo.save(r);
            auditRepo.save(new AuditLog(auth != null ? auth.getName() : "ADMIN", "RULE_UPDATED", "SocietyRule", ruleCode,
                    "Updated rule " + ruleCode + " to value " + ruleValue));
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
                                     Authentication auth,
                                     RedirectAttributes redirectAttrs) {
        interestRepo.findById(id).ifPresent(r -> {
            r.setInterestRate(rate);
            interestRepo.save(r);
            auditRepo.save(new AuditLog(auth != null ? auth.getName() : "ADMIN", "INTEREST_RATE_UPDATED", "InterestRateRule", id,
                    "Updated interest rate for " + r.getProductCode() + " to " + rate + "%"));
        });
        redirectAttrs.addFlashAttribute("successMessage", "Interest rate updated successfully.");
        return "redirect:/admin/interest-rates";
    }

    @GetMapping("/polls")
    public String redirectToPolls() {
        return "redirect:/polls";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<AuditLog> auditPage = auditRepo.findAllByOrderByTimestampDesc(PageRequest.of(page, 20));
        model.addAttribute("auditPage", auditPage);
        return "admin/audit_logs";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserAccount> users = userRepo.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("role") String role,
                             Authentication auth,
                             RedirectAttributes redirectAttrs) {
        if (userRepo.existsByUsername(username)) {
            redirectAttrs.addFlashAttribute("errorMessage", "Username '" + username + "' is already taken.");
            return "redirect:/admin/users";
        }

        Set<String> roles = new HashSet<>();
        roles.add(role);
        if ("ROLE_ADMIN".equals(role)) {
            roles.add("ROLE_STAFF");
        }

        UserAccount newUser = new UserAccount(username, passwordEncoder.encode(password), fullName, email, roles);
        userRepo.save(newUser);
        auditRepo.save(new AuditLog(auth != null ? auth.getName() : "ADMIN", "USER_PROVISIONED", "UserAccount", username,
                "Provisioned user " + username + " with role " + role));

        redirectAttrs.addFlashAttribute("successMessage", "Account for " + fullName + " (" + username + ") created successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle-status")
    public String toggleUserStatus(@RequestParam("username") String username,
                                   Authentication auth,
                                   RedirectAttributes redirectAttrs) {
        if ("admin".equalsIgnoreCase(username)) {
            redirectAttrs.addFlashAttribute("errorMessage", "Primary super-admin account cannot be disabled.");
            return "redirect:/admin/users";
        }

        userRepo.findByUsername(username).ifPresent(u -> {
            u.setEnabled(!u.isEnabled());
            userRepo.save(u);
            auditRepo.save(new AuditLog(auth != null ? auth.getName() : "ADMIN", "USER_STATUS_TOGGLED", "UserAccount", username,
                    "User status changed to enabled=" + u.isEnabled()));
            redirectAttrs.addFlashAttribute("successMessage", "User " + username + " status toggled.");
        });

        return "redirect:/admin/users";
    }
}

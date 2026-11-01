package com.amortix.prime.controller;

import com.amortix.prime.model.Poll;
import com.amortix.prime.repository.PollRepository;
import com.amortix.prime.service.SecurityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/polls")
public class PollController {

    private final PollRepository pollRepository;
    private final SecurityService securityService;

    public PollController(PollRepository pollRepository, SecurityService securityService) {
        this.pollRepository = pollRepository;
        this.securityService = securityService;
    }

    @GetMapping
    public String listPolls(Model model, Authentication auth) {
        List<Poll> polls = pollRepository.findAll();
        String voterId = securityService.getCurrentMemberCode() != null
                ? securityService.getCurrentMemberCode()
                : (auth != null ? auth.getName() : "ANONYMOUS");

        model.addAttribute("polls", polls);
        model.addAttribute("voterId", voterId);
        return "admin/polls";
    }

    @PostMapping("/vote")
    public String votePoll(@RequestParam("pollId") String pollId,
                           @RequestParam("option") String option,
                           Authentication auth,
                           RedirectAttributes redirectAttrs) {
        String voterId = securityService.getCurrentMemberCode() != null
                ? securityService.getCurrentMemberCode()
                : (auth != null ? auth.getName() : "ANONYMOUS");

        pollRepository.findById(pollId).ifPresent(p -> {
            if (!p.getMemberVotes().containsKey(voterId)) {
                p.getMemberVotes().put(voterId, option);
                p.getVoteCounts().put(option, p.getVoteCounts().getOrDefault(option, 0) + 1);
                pollRepository.save(p);
                redirectAttrs.addFlashAttribute("successMessage", "Your ballot has been successfully recorded!");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "You have already cast your ballot for this resolution.");
            }
        });

        return "redirect:/polls";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createPoll(@RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("options") List<String> options,
                             Authentication auth,
                             RedirectAttributes redirectAttrs) {
        String operator = auth != null ? auth.getName() : "ADMIN";
        List<String> cleanOptions = options.stream().filter(o -> !o.isBlank()).toList();
        Poll poll = new Poll(title, description, cleanOptions, operator);
        pollRepository.save(poll);
        redirectAttrs.addFlashAttribute("successMessage", "New AGM Resolution / Poll published successfully!");
        return "redirect:/polls";
    }
}

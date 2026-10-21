package com.amortix.prime.controller;

import com.amortix.prime.model.MonthlyRecoveryRun;
import com.amortix.prime.service.MonthlyRecoveryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/recovery")
public class RecoveryController {

    private final MonthlyRecoveryService recoveryService;

    public RecoveryController(MonthlyRecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @GetMapping
    public String recoveryDashboard(Model model) {
        List<MonthlyRecoveryRun> runs = recoveryService.listAllRuns();
        model.addAttribute("runs", runs);
        model.addAttribute("currentMonth", LocalDate.now().getMonthValue());
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "recovery/list";
    }

    @PostMapping("/generate")
    public String generateDemandSchedule(@RequestParam("month") int month,
                                         @RequestParam("year") int year,
                                         Authentication auth,
                                         RedirectAttributes redirectAttrs) {
        try {
            String officer = auth != null ? auth.getName() : "OFFICER";
            MonthlyRecoveryRun run = recoveryService.generateDemandSchedule(month, year, officer);
            redirectAttrs.addFlashAttribute("successMessage", "Demand schedule " + run.getRunCode() + " generated for INR " + run.getTotalGrossDemand());
            return "redirect:/recovery/view/" + run.getRunCode();
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to generate schedule: " + e.getMessage());
            return "redirect:/recovery";
        }
    }

    @GetMapping("/view/{runCode}")
    public String viewRun(@PathVariable String runCode, Model model) {
        MonthlyRecoveryRun run = recoveryService.findByRunCode(runCode);
        model.addAttribute("run", run);
        return "recovery/view";
    }

    @GetMapping("/export/{runCode}")
    public ResponseEntity<byte[]> exportDemandFile(@PathVariable String runCode) {
        String content = recoveryService.exportDemandScheduleAsText(runCode);
        byte[] bytes = content.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Demand_Schedule_" + runCode + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    @PostMapping("/reconcile/{runCode}")
    public String reconcileResponseFile(@PathVariable String runCode,
                                        @RequestParam("responseFile") MultipartFile file,
                                        Authentication auth,
                                        RedirectAttributes redirectAttrs) {
        try {
            if (file.isEmpty()) {
                redirectAttrs.addFlashAttribute("errorMessage", "Please select a response file to upload.");
                return "redirect:/recovery/view/" + runCode;
            }
            String officer = auth != null ? auth.getName() : "OFFICER";
            MonthlyRecoveryRun run = recoveryService.reconcilePayrollResponse(runCode, file.getInputStream(), officer);
            redirectAttrs.addFlashAttribute("successMessage", "Payroll response reconciled successfully! Recovered: INR "
                    + run.getTotalActualRecovered() + ", Discrepancies logged: " + run.getDiscrepancies().size());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Reconciliation error: " + e.getMessage());
        }
        return "redirect:/recovery/view/" + runCode;
    }
}

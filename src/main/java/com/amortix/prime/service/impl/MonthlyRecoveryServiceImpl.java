package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.LoanService;
import com.amortix.prime.service.MonthlyRecoveryService;
import com.amortix.prime.service.ThriftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class MonthlyRecoveryServiceImpl implements MonthlyRecoveryService {

    private final MonthlyRecoveryRunRepository recoveryRunRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final LoanService loanService;
    private final ThriftService thriftService;
    private final AuditLogRepository auditLogRepository;

    public MonthlyRecoveryServiceImpl(MonthlyRecoveryRunRepository recoveryRunRepository,
                                     MemberRepository memberRepository,
                                     LoanRepository loanRepository,
                                     DepositAccountRepository depositAccountRepository,
                                     LoanService loanService,
                                     ThriftService thriftService,
                                     AuditLogRepository auditLogRepository) {
        this.recoveryRunRepository = recoveryRunRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.loanService = loanService;
        this.thriftService = thriftService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public MonthlyRecoveryRun generateDemandSchedule(int month, int year, String operatorUser) {
        String runCode = String.format("REC-%04d-%02d", year, month);

        MonthlyRecoveryRun run = recoveryRunRepository.findByRunCode(runCode).orElse(new MonthlyRecoveryRun());
        run.setRunCode(runCode);
        run.setMonth(month);
        run.setYear(year);
        run.setBillingCycleName("Demand Schedule for " + LocalDate.of(year, month, 1).getMonth().name() + " " + year);
        run.setGenerationDate(LocalDate.now());
        run.setStatus("DEMAND_GENERATED");
        run.setGeneratedBy(operatorUser);

        List<Member> activeMembers = memberRepository.findByStatus("ACTIVE");
        List<MonthlyRecoveryRun.RecoveryItem> items = new ArrayList<>();

        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalThrift = BigDecimal.ZERO;
        BigDecimal totalRcd = BigDecimal.ZERO;

        for (Member m : activeMembers) {
            MonthlyRecoveryRun.RecoveryItem item = new MonthlyRecoveryRun.RecoveryItem();
            item.setMemberCode(m.getMemberCode());
            item.setMemberName(m.getFullName());
            item.setEmployeeCode(m.getEmployeeCode());
            item.setDepartment(m.getDepartment());

            // 1. Compulsory Monthly Thrift
            BigDecimal thrift = m.getMonthlyThriftAmount() != null ? m.getMonthlyThriftAmount() : new BigDecimal("1000.00");
            item.setThriftDemand(thrift);
            totalThrift = totalThrift.add(thrift);

            // 2. Active Loan Installments
            List<Loan> activeLoans = loanRepository.findByMemberCodeAndStatus(m.getMemberCode(), "ACTIVE");
            BigDecimal loanPri = BigDecimal.ZERO;
            BigDecimal loanInt = BigDecimal.ZERO;

            if (!activeLoans.isEmpty()) {
                Loan primaryLoan = activeLoans.get(0);
                item.setActiveLoanAccountNo(primaryLoan.getLoanAccountNo());

                // Reducing balance monthly interest: Outstanding * (Rate / 12) / 100
                double monthlyRate = (primaryLoan.getInterestRate() / 12.0) / 100.0;
                loanInt = primaryLoan.getOutstandingPrincipal()
                        .multiply(BigDecimal.valueOf(monthlyRate))
                        .setScale(2, RoundingMode.HALF_UP);

                // Principal portion: EMI minus Interest (or capped at outstanding)
                BigDecimal emi = primaryLoan.getMonthlyInstallment();
                loanPri = emi.subtract(loanInt).max(BigDecimal.ZERO).min(primaryLoan.getOutstandingPrincipal());

                item.setLoanPrincipalDemand(loanPri);
                item.setLoanInterestDemand(loanInt);

                totalPrincipal = totalPrincipal.add(loanPri);
                totalInterest = totalInterest.add(loanInt);
            }

            // 3. Active RCD (Recurring Deposit) Installment
            List<DepositAccount> activeRcds = depositAccountRepository.findByMemberCodeAndStatus(m.getMemberCode(), "ACTIVE");
            BigDecimal rcdDemand = BigDecimal.ZERO;
            for (DepositAccount rcd : activeRcds) {
                if ("RCD".equalsIgnoreCase(rcd.getDepositType()) && rcd.getMonthlyInstallment() != null) {
                    rcdDemand = rcdDemand.add(rcd.getMonthlyInstallment());
                }
            }
            item.setRcdDemand(rcdDemand);
            totalRcd = totalRcd.add(rcdDemand);

            BigDecimal totalMemberDemand = thrift.add(loanPri).add(loanInt).add(rcdDemand);
            item.setTotalDemand(totalMemberDemand);
            item.setStatus("PENDING");

            items.add(item);
        }

        run.setItems(items);
        run.setTotalMembersCount(items.size());
        run.setTotalPrincipalDemand(totalPrincipal);
        run.setTotalInterestDemand(totalInterest);
        run.setTotalThriftDemand(totalThrift);
        run.setTotalRcdDemand(totalRcd);
        run.setTotalGrossDemand(totalPrincipal.add(totalInterest).add(totalThrift).add(totalRcd));

        MonthlyRecoveryRun saved = recoveryRunRepository.save(run);
        auditLogRepository.save(new AuditLog(operatorUser, "DEMAND_GENERATED", "MonthlyRecoveryRun", runCode,
                "Generated demand schedule. Total Gross: ₹" + saved.getTotalGrossDemand()));
        return saved;
    }

    @Override
    public MonthlyRecoveryRun findByRunCode(String runCode) {
        return recoveryRunRepository.findByRunCode(runCode)
                .orElseThrow(() -> new IllegalArgumentException("Recovery run not found: " + runCode));
    }

    @Override
    public List<MonthlyRecoveryRun> listAllRuns() {
        return recoveryRunRepository.findAll();
    }

    @Override
    public String exportDemandScheduleAsText(String runCode) {
        MonthlyRecoveryRun run = findByRunCode(runCode);
        StringBuilder sb = new StringBuilder();
        sb.append("RUN_CODE: ").append(run.getRunCode()).append("\n");
        sb.append("CYCLE: ").append(run.getBillingCycleName()).append("\n");
        sb.append("TOTAL_MEMBERS: ").append(run.getTotalMembersCount()).append("\n");
        sb.append("TOTAL_DEMAND: ").append(run.getTotalGrossDemand()).append("\n");
        sb.append("------------------------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-10s | %-12s | %-25s | %-10s | %-10s | %-10s | %-10s\n",
                "MEMBER_CD", "EMP_CODE", "FULL_NAME", "THRIFT", "LOAN_PRI", "LOAN_INT", "TOTAL_DEMAND"));
        sb.append("------------------------------------------------------------------------------------------------------\n");

        for (MonthlyRecoveryRun.RecoveryItem it : run.getItems()) {
            sb.append(String.format("%-10s | %-12s | %-25s | %-10.2f | %-10.2f | %-10.2f | %-10.2f\n",
                    it.getMemberCode(), it.getEmployeeCode(), it.getMemberName(),
                    it.getThriftDemand(), it.getLoanPrincipalDemand(), it.getLoanInterestDemand(), it.getTotalDemand()));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public MonthlyRecoveryRun reconcilePayrollResponse(String runCode, InputStream responseFileStream, String operatorUser) {
        MonthlyRecoveryRun run = findByRunCode(runCode);

        // Read uploaded response CSV / Text file (Format: MemberCode,DeductedAmount or EmpCode,DeductedAmount)
        Map<String, BigDecimal> actualDeductions = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseFileStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("MEMBER") || line.startsWith("RUN_CODE")) {
                    continue;
                }
                String[] parts = line.split("[,|\\t]");
                if (parts.length >= 2) {
                    String code = parts[0].trim();
                    try {
                        BigDecimal amt = new BigDecimal(parts[1].trim());
                        actualDeductions.put(code, amt);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read response file: " + e.getMessage(), e);
        }

        BigDecimal totalRecovered = BigDecimal.ZERO;
        BigDecimal totalVariance = BigDecimal.ZERO;
        List<MonthlyRecoveryRun.WrongPaymentRecord> discrepancies = new ArrayList<>();

        for (MonthlyRecoveryRun.RecoveryItem item : run.getItems()) {
            BigDecimal deducted = actualDeductions.get(item.getMemberCode());
            if (deducted == null && item.getEmployeeCode() != null) {
                deducted = actualDeductions.get(item.getEmployeeCode());
            }

            if (deducted == null) {
                // Not found in response = 100% Unpaid
                deducted = BigDecimal.ZERO;
            }

            item.setActualDeducted(deducted);
            BigDecimal variance = deducted.subtract(item.getTotalDemand());
            item.setVariance(variance);
            totalRecovered = totalRecovered.add(deducted);

            if (variance.compareTo(BigDecimal.ZERO) == 0) {
                item.setStatus("MATCHED");

                // Execute automated ledger postings
                if (item.getThriftDemand().compareTo(BigDecimal.ZERO) > 0) {
                    thriftService.creditMonthlyThrift(item.getMemberCode(), item.getThriftDemand(), runCode, operatorUser);
                }
                if (item.getActiveLoanAccountNo() != null && item.getLoanPrincipalDemand().add(item.getLoanInterestDemand()).compareTo(BigDecimal.ZERO) > 0) {
                    loanService.recordLoanRepayment(item.getActiveLoanAccountNo(), item.getLoanPrincipalDemand(),
                            item.getLoanInterestDemand(), "PAYROLL_DEDUCTION", runCode, "Auto payroll recovery", operatorUser);
                }
            } else if (variance.compareTo(BigDecimal.ZERO) < 0) {
                item.setStatus(deducted.compareTo(BigDecimal.ZERO) == 0 ? "UNPAID" : "SHORT");
                totalVariance = totalVariance.add(variance.abs());

                // Record discrepancy into WrongPayment list (Legacy speccs.WrongPayment)
                discrepancies.add(new MonthlyRecoveryRun.WrongPaymentRecord(
                        item.getMemberCode(), item.getEmployeeCode(), item.getTotalDemand(),
                        deducted, variance.abs(), "Short deduction in payroll cycle " + runCode));

                // Water-fall allocation for partial deduction: Thrift first, then Interest, then Principal
                BigDecimal remaining = deducted;
                BigDecimal thriftPart = remaining.min(item.getThriftDemand());
                if (thriftPart.compareTo(BigDecimal.ZERO) > 0) {
                    thriftService.creditMonthlyThrift(item.getMemberCode(), thriftPart, runCode, operatorUser);
                    remaining = remaining.subtract(thriftPart);
                }
                BigDecimal interestPart = remaining.min(item.getLoanInterestDemand());
                remaining = remaining.subtract(interestPart);
                BigDecimal principalPart = remaining.min(item.getLoanPrincipalDemand());

                if (item.getActiveLoanAccountNo() != null && interestPart.add(principalPart).compareTo(BigDecimal.ZERO) > 0) {
                    loanService.recordLoanRepayment(item.getActiveLoanAccountNo(), principalPart, interestPart,
                            "PAYROLL_DEDUCTION", runCode, "Partial payroll recovery", operatorUser);
                }
            } else {
                item.setStatus("EXCESS");
                totalVariance = totalVariance.add(variance);
                discrepancies.add(new MonthlyRecoveryRun.WrongPaymentRecord(
                        item.getMemberCode(), item.getEmployeeCode(), item.getTotalDemand(),
                        deducted, variance, "Excess deduction in payroll cycle " + runCode));

                // Credit normal parts, excess to thrift
                if (item.getThriftDemand().compareTo(BigDecimal.ZERO) > 0) {
                    thriftService.creditMonthlyThrift(item.getMemberCode(), item.getThriftDemand().add(variance), runCode, operatorUser);
                }
                if (item.getActiveLoanAccountNo() != null) {
                    loanService.recordLoanRepayment(item.getActiveLoanAccountNo(), item.getLoanPrincipalDemand(),
                            item.getLoanInterestDemand(), "PAYROLL_DEDUCTION", runCode, "Payroll recovery", operatorUser);
                }
            }
        }

        run.setTotalActualRecovered(totalRecovered);
        run.setTotalVarianceAmount(totalVariance);
        run.setDiscrepancies(discrepancies);
        run.setStatus("RECONCILED");
        run.setResponseUploadDate(LocalDate.now());
        run.setReconciliationDate(LocalDate.now());
        run.setReconciledBy(operatorUser);

        MonthlyRecoveryRun saved = recoveryRunRepository.save(run);
        auditLogRepository.save(new AuditLog(operatorUser, "PAYROLL_RECONCILED", "MonthlyRecoveryRun", runCode,
                "Reconciled payroll. Recovered: ₹" + totalRecovered + ", Discrepancies count: " + discrepancies.size()));

        return saved;
    }
}

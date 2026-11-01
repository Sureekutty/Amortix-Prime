package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.DepositService;
import com.amortix.prime.service.LoanService;
import com.amortix.prime.service.MonthlyRecoveryService;
import com.amortix.prime.service.ThriftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class MonthlyRecoveryServiceImpl implements MonthlyRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(MonthlyRecoveryServiceImpl.class);

    private final MonthlyRecoveryRunRepository recoveryRunRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final LoanService loanService;
    private final ThriftService thriftService;
    private final DepositService depositService;
    private final AuditLogRepository auditLogRepository;

    public MonthlyRecoveryServiceImpl(MonthlyRecoveryRunRepository recoveryRunRepository,
                                      MemberRepository memberRepository,
                                      LoanRepository loanRepository,
                                      DepositAccountRepository depositAccountRepository,
                                      LoanService loanService,
                                      ThriftService thriftService,
                                      DepositService depositService,
                                      AuditLogRepository auditLogRepository) {
        this.recoveryRunRepository = recoveryRunRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.loanService = loanService;
        this.thriftService = thriftService;
        this.depositService = depositService;
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

            // 2. Active Loan Installments (Iterate over ALL active loans for member - Bug #23 fix)
            List<Loan> activeLoans = loanRepository.findByMemberCodeAndStatus(m.getMemberCode(), "ACTIVE");
            BigDecimal memberTotalLoanPri = BigDecimal.ZERO;
            BigDecimal memberTotalLoanInt = BigDecimal.ZERO;
            StringBuilder loanAccs = new StringBuilder();

            for (Loan loan : activeLoans) {
                double monthlyRate = (loan.getInterestRate() / 12.0) / 100.0;
                BigDecimal singleLoanInt = loan.getOutstandingPrincipal()
                        .multiply(BigDecimal.valueOf(monthlyRate))
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal emi = loan.getMonthlyInstallment() != null ? loan.getMonthlyInstallment() : BigDecimal.ZERO;
                BigDecimal singleLoanPri = emi.subtract(singleLoanInt).max(BigDecimal.ZERO).min(loan.getOutstandingPrincipal());

                memberTotalLoanInt = memberTotalLoanInt.add(singleLoanInt);
                memberTotalLoanPri = memberTotalLoanPri.add(singleLoanPri);

                if (loanAccs.length() > 0) loanAccs.append(",");
                loanAccs.append(loan.getLoanAccountNo());
            }

            item.setActiveLoanAccountNo(loanAccs.length() > 0 ? loanAccs.toString() : null);
            item.setLoanPrincipalDemand(memberTotalLoanPri);
            item.setLoanInterestDemand(memberTotalLoanInt);

            totalPrincipal = totalPrincipal.add(memberTotalLoanPri);
            totalInterest = totalInterest.add(memberTotalLoanInt);

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

            BigDecimal totalMemberDemand = thrift.add(memberTotalLoanPri).add(memberTotalLoanInt).add(rcdDemand);
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

        // Prevent duplicate reconciliation (Bug #2 fix)
        if ("RECONCILED".equalsIgnoreCase(run.getStatus())) {
            throw new IllegalStateException("Recovery schedule [" + runCode + "] has already been reconciled and posted.");
        }

        // Read uploaded response CSV / Text file with UTF-8 (Bug #26 fix)
        Map<String, BigDecimal> actualDeductions = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseFileStream, StandardCharsets.UTF_8))) {
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
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid deduction amount in response file for code {}: {}", code, parts[1]);
                    }
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

            // Unique receipt number per member (Bug #1 fix)
            String memberReceiptNo = String.format("REC-%s-%s", runCode, item.getMemberCode());

            if (variance.compareTo(BigDecimal.ZERO) == 0) {
                item.setStatus("MATCHED");

                // 1. Thrift posting
                if (item.getThriftDemand().compareTo(BigDecimal.ZERO) > 0) {
                    thriftService.creditMonthlyThrift(item.getMemberCode(), item.getThriftDemand(), runCode, operatorUser);
                }

                // 2. Loan repayment posting
                postLoanRepayments(item.getMemberCode(), item.getLoanPrincipalDemand(), item.getLoanInterestDemand(),
                        memberReceiptNo, "Auto payroll recovery", operatorUser);

                // 3. RCD installment posting (Bug #3 fix)
                postRcdInstallment(item.getMemberCode(), item.getRcdDemand(), memberReceiptNo, operatorUser);

            } else if (variance.compareTo(BigDecimal.ZERO) < 0) {
                item.setStatus(deducted.compareTo(BigDecimal.ZERO) == 0 ? "UNPAID" : "SHORT");
                totalVariance = totalVariance.add(variance.abs());

                discrepancies.add(new MonthlyRecoveryRun.WrongPaymentRecord(
                        item.getMemberCode(), item.getEmployeeCode(), item.getTotalDemand(),
                        deducted, variance.abs(), "Short deduction in payroll cycle " + runCode));

                // Water-fall allocation for partial deduction: Thrift first, then RCD, then Loan Interest, then Loan Principal
                BigDecimal remaining = deducted;

                // A. Thrift
                BigDecimal thriftPart = remaining.min(item.getThriftDemand());
                if (thriftPart.compareTo(BigDecimal.ZERO) > 0) {
                    thriftService.creditMonthlyThrift(item.getMemberCode(), thriftPart, runCode, operatorUser);
                    remaining = remaining.subtract(thriftPart);
                }

                // B. RCD (Bug #3 fix)
                BigDecimal rcdPart = remaining.min(item.getRcdDemand());
                if (rcdPart.compareTo(BigDecimal.ZERO) > 0) {
                    postRcdInstallment(item.getMemberCode(), rcdPart, memberReceiptNo, operatorUser);
                    remaining = remaining.subtract(rcdPart);
                }

                // C. Loan Interest
                BigDecimal interestPart = remaining.min(item.getLoanInterestDemand());
                remaining = remaining.subtract(interestPart);

                // D. Loan Principal
                BigDecimal principalPart = remaining.min(item.getLoanPrincipalDemand());

                if (interestPart.add(principalPart).compareTo(BigDecimal.ZERO) > 0) {
                    postLoanRepayments(item.getMemberCode(), principalPart, interestPart,
                            memberReceiptNo, "Partial payroll recovery", operatorUser);
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
                postLoanRepayments(item.getMemberCode(), item.getLoanPrincipalDemand(), item.getLoanInterestDemand(),
                        memberReceiptNo, "Payroll recovery", operatorUser);
                postRcdInstallment(item.getMemberCode(), item.getRcdDemand(), memberReceiptNo, operatorUser);
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

    private void postLoanRepayments(String memberCode, BigDecimal principal, BigDecimal interest,
                                    String receiptNo, String remarks, String operatorUser) {
        if (principal.add(interest).compareTo(BigDecimal.ZERO) <= 0) return;

        List<Loan> activeLoans = loanRepository.findByMemberCodeAndStatus(memberCode, "ACTIVE");
        if (activeLoans.isEmpty()) return;

        // Post to primary active loan (or first in list)
        Loan loan = activeLoans.get(0);
        loanService.recordLoanRepayment(loan.getLoanAccountNo(), principal, interest,
                "PAYROLL_DEDUCTION", receiptNo, remarks, operatorUser);
    }

    private void postRcdInstallment(String memberCode, BigDecimal rcdAmount, String receiptNo, String operatorUser) {
        if (rcdAmount == null || rcdAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        List<DepositAccount> activeRcds = depositAccountRepository.findByMemberCodeAndStatus(memberCode, "ACTIVE");
        for (DepositAccount rcd : activeRcds) {
            if ("RCD".equalsIgnoreCase(rcd.getDepositType())) {
                depositService.recordRcdInstallment(rcd.getDepositAccountNo(), rcdAmount, receiptNo, operatorUser);
                break;
            }
        }
    }
}

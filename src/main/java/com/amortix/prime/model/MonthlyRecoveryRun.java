package com.amortix.prime.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Monthly Recovery Run Entity.
 * Core Cooperative Engine for generating monthly payroll deduction demand schedules
 * and reconciling payroll response files.
 * Corresponds to Amortix MonthlyProcess, amortix.MonthlyResponse, amortix.ProcessMonth.
 */
@Entity
@Table(name = "monthly_recovery_runs")
public class MonthlyRecoveryRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String runCode; // e.g. REC-2026-10

    private int month; // 1 to 12
    private int year;  // e.g. 2026
    private String billingCycleName; // e.g. "October 2026 Salary Deductions"

    private LocalDate generationDate = LocalDate.now();
    private LocalDate responseUploadDate;
    private LocalDate reconciliationDate;

    // Status: DEMAND_GENERATED, RESPONSE_UPLOADED, RECONCILED, FINALIZED
    private String status = "DEMAND_GENERATED";

    private int totalMembersCount = 0;
    private BigDecimal totalPrincipalDemand = BigDecimal.ZERO;
    private BigDecimal totalInterestDemand = BigDecimal.ZERO;
    private BigDecimal totalThriftDemand = BigDecimal.ZERO;
    private BigDecimal totalRcdDemand = BigDecimal.ZERO;
    private BigDecimal totalGrossDemand = BigDecimal.ZERO;

    private BigDecimal totalActualRecovered = BigDecimal.ZERO;
    private BigDecimal totalVarianceAmount = BigDecimal.ZERO; // Under/Over deductions

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recovery_run_items", joinColumns = @JoinColumn(name = "recovery_run_id"))
    private List<RecoveryItem> items = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recovery_run_discrepancies", joinColumns = @JoinColumn(name = "recovery_run_id"))
    private List<WrongPaymentRecord> discrepancies = new ArrayList<>();

    private String generatedBy;
    private String reconciledBy;
    private LocalDateTime createdDate = LocalDateTime.now();

    public MonthlyRecoveryRun() {}

    @Embeddable
    public static class RecoveryItem {
        private String memberCode;
        private String memberName;
        private String employeeCode;
        private String department;

        private String activeLoanAccountNo;
        private BigDecimal loanPrincipalDemand = BigDecimal.ZERO;
        private BigDecimal loanInterestDemand = BigDecimal.ZERO;
        private BigDecimal thriftDemand = BigDecimal.ZERO;
        private BigDecimal rcdDemand = BigDecimal.ZERO;
        private BigDecimal totalDemand = BigDecimal.ZERO;

        private BigDecimal actualDeducted = BigDecimal.ZERO;
        private BigDecimal variance = BigDecimal.ZERO;
        private String status = "PENDING"; // PENDING, MATCHED, SHORT, UNPAID, EXCESS
        private String discrepancyReason;

        public RecoveryItem() {}

        public String getMemberCode() { return memberCode; }
        public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getActiveLoanAccountNo() { return activeLoanAccountNo; }
        public void setActiveLoanAccountNo(String activeLoanAccountNo) { this.activeLoanAccountNo = activeLoanAccountNo; }
        public BigDecimal getLoanPrincipalDemand() { return loanPrincipalDemand; }
        public void setLoanPrincipalDemand(BigDecimal loanPrincipalDemand) { this.loanPrincipalDemand = loanPrincipalDemand; }
        public BigDecimal getLoanInterestDemand() { return loanInterestDemand; }
        public void setLoanInterestDemand(BigDecimal loanInterestDemand) { this.loanInterestDemand = loanInterestDemand; }
        public BigDecimal getThriftDemand() { return thriftDemand; }
        public void setThriftDemand(BigDecimal thriftDemand) { this.thriftDemand = thriftDemand; }
        public BigDecimal getRcdDemand() { return rcdDemand; }
        public void setRcdDemand(BigDecimal rcdDemand) { this.rcdDemand = rcdDemand; }
        public BigDecimal getTotalDemand() { return totalDemand; }
        public void setTotalDemand(BigDecimal totalDemand) { this.totalDemand = totalDemand; }
        public BigDecimal getActualDeducted() { return actualDeducted; }
        public void setActualDeducted(BigDecimal actualDeducted) { this.actualDeducted = actualDeducted; }
        public BigDecimal getVariance() { return variance; }
        public void setVariance(BigDecimal variance) { this.variance = variance; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDiscrepancyReason() { return discrepancyReason; }
        public void setDiscrepancyReason(String discrepancyReason) { this.discrepancyReason = discrepancyReason; }
    }

    @Embeddable
    public static class WrongPaymentRecord {
        private String memberCode;
        private String employeeCode;
        private BigDecimal demandAmount = BigDecimal.ZERO;
        private BigDecimal deductedAmount = BigDecimal.ZERO;
        private BigDecimal shortOrExcessAmount = BigDecimal.ZERO;
        private String remarks;

        public WrongPaymentRecord() {}

        public WrongPaymentRecord(String memberCode, String employeeCode, BigDecimal demandAmount, BigDecimal deductedAmount, BigDecimal shortOrExcessAmount, String remarks) {
            this.memberCode = memberCode;
            this.employeeCode = employeeCode;
            this.demandAmount = demandAmount;
            this.deductedAmount = deductedAmount;
            this.shortOrExcessAmount = shortOrExcessAmount;
            this.remarks = remarks;
        }

        public String getMemberCode() { return memberCode; }
        public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public BigDecimal getDemandAmount() { return demandAmount; }
        public void setDemandAmount(BigDecimal demandAmount) { this.demandAmount = demandAmount; }
        public BigDecimal getDeductedAmount() { return deductedAmount; }
        public void setDeductedAmount(BigDecimal deductedAmount) { this.deductedAmount = deductedAmount; }
        public BigDecimal getShortOrExcessAmount() { return shortOrExcessAmount; }
        public void setShortOrExcessAmount(BigDecimal shortOrExcessAmount) { this.shortOrExcessAmount = shortOrExcessAmount; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRunCode() { return runCode; }
    public void setRunCode(String runCode) { this.runCode = runCode; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getBillingCycleName() { return billingCycleName; }
    public void setBillingCycleName(String billingCycleName) { this.billingCycleName = billingCycleName; }
    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate generationDate) { this.generationDate = generationDate; }
    public LocalDate getResponseUploadDate() { return responseUploadDate; }
    public void setResponseUploadDate(LocalDate responseUploadDate) { this.responseUploadDate = responseUploadDate; }
    public LocalDate getReconciliationDate() { return reconciliationDate; }
    public void setReconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalMembersCount() { return totalMembersCount; }
    public void setTotalMembersCount(int totalMembersCount) { this.totalMembersCount = totalMembersCount; }
    public BigDecimal getTotalPrincipalDemand() { return totalPrincipalDemand; }
    public void setTotalPrincipalDemand(BigDecimal totalPrincipalDemand) { this.totalPrincipalDemand = totalPrincipalDemand; }
    public BigDecimal getTotalInterestDemand() { return totalInterestDemand; }
    public void setTotalInterestDemand(BigDecimal totalInterestDemand) { this.totalInterestDemand = totalInterestDemand; }
    public BigDecimal getTotalThriftDemand() { return totalThriftDemand; }
    public void setTotalThriftDemand(BigDecimal totalThriftDemand) { this.totalThriftDemand = totalThriftDemand; }
    public BigDecimal getTotalRcdDemand() { return totalRcdDemand; }
    public void setTotalRcdDemand(BigDecimal totalRcdDemand) { this.totalRcdDemand = totalRcdDemand; }
    public BigDecimal getTotalGrossDemand() { return totalGrossDemand; }
    public void setTotalGrossDemand(BigDecimal totalGrossDemand) { this.totalGrossDemand = totalGrossDemand; }
    public BigDecimal getTotalActualRecovered() { return totalActualRecovered; }
    public void setTotalActualRecovered(BigDecimal totalActualRecovered) { this.totalActualRecovered = totalActualRecovered; }
    public BigDecimal getTotalVarianceAmount() { return totalVarianceAmount; }
    public void setTotalVarianceAmount(BigDecimal totalVarianceAmount) { this.totalVarianceAmount = totalVarianceAmount; }
    public List<RecoveryItem> getItems() { return items; }
    public void setItems(List<RecoveryItem> items) { this.items = items; }
    public List<WrongPaymentRecord> getDiscrepancies() { return discrepancies; }
    public void setDiscrepancies(List<WrongPaymentRecord> discrepancies) { this.discrepancies = discrepancies; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public String getReconciledBy() { return reconciledBy; }
    public void setReconciledBy(String reconciledBy) { this.reconciledBy = reconciledBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

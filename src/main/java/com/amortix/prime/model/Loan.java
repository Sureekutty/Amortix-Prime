package com.amortix.prime.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Loan Entity.
 * Corresponds to Amortix Loans, amortix.Loanstatus, amortix.Surety.
 * Supports products: LTL, EXL, ELG, FDL.
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String loanAccountNo; // e.g. LTL-10001, EXL-20001, ELG-30001, FDL-40001

    private String memberCode;
    private String memberName;
    private String employeeCode;
    private String department;

    private String loanType; // LTL, EXL, ELG, FDL
    private String loanPurpose;

    private BigDecimal sanctionAmount = BigDecimal.ZERO;
    private Double interestRate = 9.0; // annual percentage
    private Integer tenureMonths = 12;
    private BigDecimal monthlyInstallment = BigDecimal.ZERO; // EMI or Principal Installment
    private String interestMethod = "REDUCING_BALANCE"; // REDUCING_BALANCE, FLAT, SIMPLE

    private LocalDate applicationDate = LocalDate.now();
    private LocalDate sanctionDate;
    private LocalDate disbursementDate;
    private LocalDate recoveryStartDate;
    private LocalDate closedDate;

    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;
    private BigDecimal outstandingInterest = BigDecimal.ZERO;
    private BigDecimal totalPrincipalRepaid = BigDecimal.ZERO;
    private BigDecimal totalInterestRepaid = BigDecimal.ZERO;

    // Status: APPLIED, APPROVED, SANCTIONED, DISBURSED, ACTIVE, REJECTED, CLOSED
    private String status = "APPLIED";

    // Surety details (Up to 3 guarantors)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "loan_sureties", joinColumns = @JoinColumn(name = "loan_id"))
    private List<LoanSurety> sureties = new ArrayList<>();

    // Collateral (used especially for FDL - Fixed Deposit Loan)
    private String collateralDepositNo;
    private BigDecimal collateralDepositAmount = BigDecimal.ZERO;

    // Cheque / Disbursed Bank info
    private String disbursementMode = "BANK_TRANSFER"; // CASH, CHEQUE, BANK_TRANSFER
    private String chequeOrRefNo;
    private String sanctionedBy;
    @Column(length = 1000)
    private String remarks;

    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime updatedDate = LocalDateTime.now();

    public Loan() {}

    @Embeddable
    public static class LoanSurety {
        private String suretyMemberCode;
        private String suretyMemberName;
        private String suretyEmployeeCode;
        private BigDecimal suretyBasicPay = BigDecimal.ZERO;
        private LocalDate agreedDate = LocalDate.now();
        private String status = "CONFIRMED"; // CONFIRMED, REPLACED

        public LoanSurety() {}

        public LoanSurety(String suretyMemberCode, String suretyMemberName, String suretyEmployeeCode, BigDecimal suretyBasicPay) {
            this.suretyMemberCode = suretyMemberCode;
            this.suretyMemberName = suretyMemberName;
            this.suretyEmployeeCode = suretyEmployeeCode;
            this.suretyBasicPay = suretyBasicPay;
        }

        public String getSuretyMemberCode() { return suretyMemberCode; }
        public void setSuretyMemberCode(String suretyMemberCode) { this.suretyMemberCode = suretyMemberCode; }
        public String getSuretyMemberName() { return suretyMemberName; }
        public void setSuretyMemberName(String suretyMemberName) { this.suretyMemberName = suretyMemberName; }
        public String getSuretyEmployeeCode() { return suretyEmployeeCode; }
        public void setSuretyEmployeeCode(String suretyEmployeeCode) { this.suretyEmployeeCode = suretyEmployeeCode; }
        public BigDecimal getSuretyBasicPay() { return suretyBasicPay; }
        public void setSuretyBasicPay(BigDecimal suretyBasicPay) { this.suretyBasicPay = suretyBasicPay; }
        public LocalDate getAgreedDate() { return agreedDate; }
        public void setAgreedDate(LocalDate agreedDate) { this.agreedDate = agreedDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLoanAccountNo() { return loanAccountNo; }
    public void setLoanAccountNo(String loanAccountNo) { this.loanAccountNo = loanAccountNo; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }
    public BigDecimal getSanctionAmount() { return sanctionAmount; }
    public void setSanctionAmount(BigDecimal sanctionAmount) { this.sanctionAmount = sanctionAmount; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public BigDecimal getMonthlyInstallment() { return monthlyInstallment; }
    public void setMonthlyInstallment(BigDecimal monthlyInstallment) { this.monthlyInstallment = monthlyInstallment; }
    public String getInterestMethod() { return interestMethod; }
    public void setInterestMethod(String interestMethod) { this.interestMethod = interestMethod; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    public LocalDate getSanctionDate() { return sanctionDate; }
    public void setSanctionDate(LocalDate sanctionDate) { this.sanctionDate = sanctionDate; }
    public LocalDate getDisbursementDate() { return disbursementDate; }
    public void setDisbursementDate(LocalDate disbursementDate) { this.disbursementDate = disbursementDate; }
    public LocalDate getRecoveryStartDate() { return recoveryStartDate; }
    public void setRecoveryStartDate(LocalDate recoveryStartDate) { this.recoveryStartDate = recoveryStartDate; }
    public LocalDate getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDate closedDate) { this.closedDate = closedDate; }
    public BigDecimal getOutstandingPrincipal() { return outstandingPrincipal; }
    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) { this.outstandingPrincipal = outstandingPrincipal; }
    public BigDecimal getOutstandingInterest() { return outstandingInterest; }
    public void setOutstandingInterest(BigDecimal outstandingInterest) { this.outstandingInterest = outstandingInterest; }
    public BigDecimal getTotalPrincipalRepaid() { return totalPrincipalRepaid; }
    public void setTotalPrincipalRepaid(BigDecimal totalPrincipalRepaid) { this.totalPrincipalRepaid = totalPrincipalRepaid; }
    public BigDecimal getTotalInterestRepaid() { return totalInterestRepaid; }
    public void setTotalInterestRepaid(BigDecimal totalInterestRepaid) { this.totalInterestRepaid = totalInterestRepaid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<LoanSurety> getSureties() { return sureties; }
    public void setSureties(List<LoanSurety> sureties) { this.sureties = sureties; }
    public String getCollateralDepositNo() { return collateralDepositNo; }
    public void setCollateralDepositNo(String collateralDepositNo) { this.collateralDepositNo = collateralDepositNo; }
    public BigDecimal getCollateralDepositAmount() { return collateralDepositAmount; }
    public void setCollateralDepositAmount(BigDecimal collateralDepositAmount) { this.collateralDepositAmount = collateralDepositAmount; }
    public String getDisbursementMode() { return disbursementMode; }
    public void setDisbursementMode(String disbursementMode) { this.disbursementMode = disbursementMode; }
    public String getChequeOrRefNo() { return chequeOrRefNo; }
    public void setChequeOrRefNo(String chequeOrRefNo) { this.chequeOrRefNo = chequeOrRefNo; }
    public String getSanctionedBy() { return sanctionedBy; }
    public void setSanctionedBy(String sanctionedBy) { this.sanctionedBy = sanctionedBy; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}

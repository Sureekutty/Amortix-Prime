package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deposit Account Document.
 * Corresponds to Amortix Deposits, amortix.DepositTransactions, amortix.SP_Calculations.
 * Supports products: FXD (Fixed Deposit), RCD (Recurring Deposit), MIS (Monthly Income Scheme).
 */
@Document(collection = "deposit_accounts")
public class DepositAccount {

    @Id
    private String id;

    @Indexed(unique = true)
    private String depositAccountNo; // e.g. FXD-10001, RCD-20001, MIS-30001

    @Indexed
    private String memberCode;
    private String memberName;

    private String depositType; // FXD, RCD, MIS
    private BigDecimal principalAmount = BigDecimal.ZERO;
    private BigDecimal monthlyInstallment = BigDecimal.ZERO; // for RCD
    private Integer tenureMonths = 12;
    private Double interestRate = 7.5; // annual percentage
    private String compoundingFrequency = "QUARTERLY"; // SIMPLE, MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY

    private LocalDate depositDate = LocalDate.now();
    private LocalDate maturityDate;
    private BigDecimal maturityAmount = BigDecimal.ZERO;
    private BigDecimal accumulatedInterest = BigDecimal.ZERO;
    private BigDecimal totalPaidIn = BigDecimal.ZERO;

    // Status: ACTIVE, MATURED, PREMATURE_CLOSED, LIEN_MARKED
    private String status = "ACTIVE";

    // Lien marking if pledged as collateral for FDL loan
    private boolean lienMarked = false;
    private String lienLoanAccountNo;

    // Settlement / closure details
    private LocalDate closureDate;
    private BigDecimal prematurePenaltyRate = BigDecimal.ZERO;
    private BigDecimal finalPayoutAmount = BigDecimal.ZERO;
    private String payoutMode = "BANK_TRANSFER"; // CASH, BANK_TRANSFER
    private String payoutVoucherNo;

    private String remarks;
    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime updatedDate = LocalDateTime.now();

    public DepositAccount() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepositAccountNo() { return depositAccountNo; }
    public void setDepositAccountNo(String depositAccountNo) { this.depositAccountNo = depositAccountNo; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getDepositType() { return depositType; }
    public void setDepositType(String depositType) { this.depositType = depositType; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public BigDecimal getMonthlyInstallment() { return monthlyInstallment; }
    public void setMonthlyInstallment(BigDecimal monthlyInstallment) { this.monthlyInstallment = monthlyInstallment; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public String getCompoundingFrequency() { return compoundingFrequency; }
    public void setCompoundingFrequency(String compoundingFrequency) { this.compoundingFrequency = compoundingFrequency; }
    public LocalDate getDepositDate() { return depositDate; }
    public void setDepositDate(LocalDate depositDate) { this.depositDate = depositDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    public BigDecimal getMaturityAmount() { return maturityAmount; }
    public void setMaturityAmount(BigDecimal maturityAmount) { this.maturityAmount = maturityAmount; }
    public BigDecimal getAccumulatedInterest() { return accumulatedInterest; }
    public void setAccumulatedInterest(BigDecimal accumulatedInterest) { this.accumulatedInterest = accumulatedInterest; }
    public BigDecimal getTotalPaidIn() { return totalPaidIn; }
    public void setTotalPaidIn(BigDecimal totalPaidIn) { this.totalPaidIn = totalPaidIn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isLienMarked() { return lienMarked; }
    public void setLienMarked(boolean lienMarked) { this.lienMarked = lienMarked; }
    public String getLienLoanAccountNo() { return lienLoanAccountNo; }
    public void setLienLoanAccountNo(String lienLoanAccountNo) { this.lienLoanAccountNo = lienLoanAccountNo; }
    public LocalDate getClosureDate() { return closureDate; }
    public void setClosureDate(LocalDate closureDate) { this.closureDate = closureDate; }
    public BigDecimal getPrematurePenaltyRate() { return prematurePenaltyRate; }
    public void setPrematurePenaltyRate(BigDecimal prematurePenaltyRate) { this.prematurePenaltyRate = prematurePenaltyRate; }
    public BigDecimal getFinalPayoutAmount() { return finalPayoutAmount; }
    public void setFinalPayoutAmount(BigDecimal finalPayoutAmount) { this.finalPayoutAmount = finalPayoutAmount; }
    public String getPayoutMode() { return payoutMode; }
    public void setPayoutMode(String payoutMode) { this.payoutMode = payoutMode; }
    public String getPayoutVoucherNo() { return payoutVoucherNo; }
    public void setPayoutVoucherNo(String payoutVoucherNo) { this.payoutVoucherNo = payoutVoucherNo; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}

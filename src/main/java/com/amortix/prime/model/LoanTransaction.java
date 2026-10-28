package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Loan Transaction Document.
 * Corresponds to Amortix LoanTransactions.
 */
@Document(collection = "loan_transactions")
public class LoanTransaction {

    @Id
    private String id;

    @Indexed
    private String loanAccountNo;

    @Indexed
    private String memberCode;

    private LocalDate txnDate = LocalDate.now();
    private String txnType; // DISBURSEMENT, REPAYMENT, PREPAYMENT, ADJUSTMENT
    private String purposeCode; // L23, L28 etc from legacy
    private BigDecimal principalAmount = BigDecimal.ZERO;
    private BigDecimal interestAmount = BigDecimal.ZERO;
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private BigDecimal balancePrincipalAfterTxn = BigDecimal.ZERO;
    private BigDecimal balanceInterestAfterTxn = BigDecimal.ZERO;

    private String paymentMode = "PAYROLL_DEDUCTION"; // PAYROLL_DEDUCTION, CASH, BANK_TRANSFER, CHEQUE
    private String receiptNo;
    private String voucherNo;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate = LocalDateTime.now();

    public LoanTransaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLoanAccountNo() { return loanAccountNo; }
    public void setLoanAccountNo(String loanAccountNo) { this.loanAccountNo = loanAccountNo; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public LocalDate getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public String getPurposeCode() { return purposeCode; }
    public void setPurposeCode(String purposeCode) { this.purposeCode = purposeCode; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getBalancePrincipalAfterTxn() { return balancePrincipalAfterTxn; }
    public void setBalancePrincipalAfterTxn(BigDecimal balancePrincipalAfterTxn) { this.balancePrincipalAfterTxn = balancePrincipalAfterTxn; }
    public BigDecimal getBalanceInterestAfterTxn() { return balanceInterestAfterTxn; }
    public void setBalanceInterestAfterTxn(BigDecimal balanceInterestAfterTxn) { this.balanceInterestAfterTxn = balanceInterestAfterTxn; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

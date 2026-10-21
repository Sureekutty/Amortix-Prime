package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deposit Transaction Document.
 * Corresponds to legacy speccs.DepositTransactions.
 */
@Document(collection = "deposit_transactions")
public class DepositTransaction {

    @Id
    private String id;

    @Indexed
    private String depositAccountNo;

    @Indexed
    private String memberCode;

    private LocalDate txnDate = LocalDate.now();
    private String txnType; // OPENING, INSTALLMENT, INTEREST_ACCRUAL, INTEREST_PAYOUT, MATURITY_PAYOUT, PREMATURE_CLOSURE
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal balanceAfterTxn = BigDecimal.ZERO;
    private String voucherOrReceiptNo;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate = LocalDateTime.now();

    public DepositTransaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepositAccountNo() { return depositAccountNo; }
    public void setDepositAccountNo(String depositAccountNo) { this.depositAccountNo = depositAccountNo; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public LocalDate getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfterTxn() { return balanceAfterTxn; }
    public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) { this.balanceAfterTxn = balanceAfterTxn; }
    public String getVoucherOrReceiptNo() { return voucherOrReceiptNo; }
    public void setVoucherOrReceiptNo(String voucherOrReceiptNo) { this.voucherOrReceiptNo = voucherOrReceiptNo; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

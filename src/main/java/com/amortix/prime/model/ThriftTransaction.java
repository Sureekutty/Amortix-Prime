package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Thrift Transaction Document.
 * Corresponds to Amortix ThriftTransactions, amortix.ThriftInterestAmnt, amortix.sp_ThriftProcessUpload.
 */
@Document(collection = "thrift_transactions")
public class ThriftTransaction {

    @Id
    private String id;

    @Indexed
    private String memberCode;
    private String memberName;

    private LocalDate txnDate = LocalDate.now();
    private String txnType; // MONTHLY_SUBSCRIPTION, INTEREST_CREDIT, SETTLEMENT_PAYOUT, ADJUSTMENT
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal balanceAfterTxn = BigDecimal.ZERO;

    private String recoveryRunId; // Associated monthly recovery run if collected via payroll
    private String voucherOrReceiptNo;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate = LocalDateTime.now();

    public ThriftTransaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public LocalDate getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfterTxn() { return balanceAfterTxn; }
    public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) { this.balanceAfterTxn = balanceAfterTxn; }
    public String getRecoveryRunId() { return recoveryRunId; }
    public void setRecoveryRunId(String recoveryRunId) { this.recoveryRunId = recoveryRunId; }
    public String getVoucherOrReceiptNo() { return voucherOrReceiptNo; }
    public void setVoucherOrReceiptNo(String voucherOrReceiptNo) { this.voucherOrReceiptNo = voucherOrReceiptNo; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

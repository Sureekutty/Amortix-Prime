package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Voucher Document.
 * Unified double-entry transaction record supporting Receipts, Payments, Journal Vouchers (JV),
 * and Miscellaneous society expenses.
 * Corresponds to Amortix Receipts, amortix.Payments, amortix.PayVouchers, amortix.MiscellaneousPayment.
 */
@Document(collection = "vouchers")
public class Voucher {

    @Id
    private String id;

    @Indexed(unique = true)
    private String voucherNo; // e.g. RCP-2026-0001, PMT-2026-0001, JV-2026-0001

    private String voucherType; // RECEIPT, PAYMENT, JOURNAL_VOUCHER, MISC_EXPENSE
    private LocalDate voucherDate = LocalDate.now();

    @Indexed
    private String memberCode; // Optional if transaction is member-related
    private String memberName;

    private String debitAccountCode; // e.g. 1001-CASH, 1002-SBI_BANK, 2001-LOANS_LTL
    private String debitAccountName;

    private String creditAccountCode; // e.g. 3001-THRIFT_SUBSCRIPTION, 4001-SHARE_CAPITAL
    private String creditAccountName;

    private BigDecimal amount = BigDecimal.ZERO;

    private String paymentMode = "CASH"; // CASH, BANK_TRANSFER, CHEQUE, ADJUSTMENT
    private String instrumentNo; // Cheque number, UTR number
    private LocalDate instrumentDate;

    private String narration; // Purpose / Particulars
    private String category; // LOAN_DISBURSEMENT, LOAN_RECOVERY, DEPOSIT_OPENING, THRIFT_CREDIT, ADMIN_EXPENSE

    private String preparedBy;
    private String approvedBy;
    private String status = "POSTED"; // DRAFT, POSTED, CANCELLED

    private LocalDateTime createdDate = LocalDateTime.now();

    public Voucher() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }
    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }
    public LocalDate getVoucherDate() { return voucherDate; }
    public void setVoucherDate(LocalDate voucherDate) { this.voucherDate = voucherDate; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getDebitAccountCode() { return debitAccountCode; }
    public void setDebitAccountCode(String debitAccountCode) { this.debitAccountCode = debitAccountCode; }
    public String getDebitAccountName() { return debitAccountName; }
    public void setDebitAccountName(String debitAccountName) { this.debitAccountName = debitAccountName; }
    public String getCreditAccountCode() { return creditAccountCode; }
    public void setCreditAccountCode(String creditAccountCode) { this.creditAccountCode = creditAccountCode; }
    public String getCreditAccountName() { return creditAccountName; }
    public void setCreditAccountName(String creditAccountName) { this.creditAccountName = creditAccountName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getInstrumentNo() { return instrumentNo; }
    public void setInstrumentNo(String instrumentNo) { this.instrumentNo = instrumentNo; }
    public LocalDate getInstrumentDate() { return instrumentDate; }
    public void setInstrumentDate(LocalDate instrumentDate) { this.instrumentDate = instrumentDate; }
    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPreparedBy() { return preparedBy; }
    public void setPreparedBy(String preparedBy) { this.preparedBy = preparedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

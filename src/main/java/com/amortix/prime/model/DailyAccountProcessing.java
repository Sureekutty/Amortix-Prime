package com.amortix.prime.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily Accounts Processing Entity (Day Book Master).
 * Ensures everyday cash and bank balances are reconciled, tallied, and locked.
 * Corresponds to Amortix DailyAccountsProcessing and DailyAccountsProcessing.java.
 */
@Entity
@Table(name = "daily_accounts_processing")
public class DailyAccountProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private LocalDate processingDate; // Day book date

    private BigDecimal openingCashBalance = BigDecimal.ZERO;
    private BigDecimal openingBankBalance = BigDecimal.ZERO;

    private BigDecimal totalCashReceipts = BigDecimal.ZERO;
    private BigDecimal totalBankReceipts = BigDecimal.ZERO;

    private BigDecimal totalCashPayments = BigDecimal.ZERO;
    private BigDecimal totalBankPayments = BigDecimal.ZERO;

    private BigDecimal closingCashBalance = BigDecimal.ZERO;
    private BigDecimal closingBankBalance = BigDecimal.ZERO;

    // Status: OPEN, CLOSED, AUDITED, LOCKED
    private String dayStatus = "OPEN";
    private String closedBy;
    private LocalDateTime closedTime;
    @Column(length = 1000)
    private String remarks;

    private LocalDateTime createdDate = LocalDateTime.now();

    public DailyAccountProcessing() {}

    public DailyAccountProcessing(LocalDate processingDate, BigDecimal openingCash, BigDecimal openingBank) {
        this.processingDate = processingDate;
        this.openingCashBalance = openingCash;
        this.openingBankBalance = openingBank;
        this.closingCashBalance = openingCash;
        this.closingBankBalance = openingBank;
        this.dayStatus = "OPEN";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getProcessingDate() { return processingDate; }
    public void setProcessingDate(LocalDate processingDate) { this.processingDate = processingDate; }
    public BigDecimal getOpeningCashBalance() { return openingCashBalance; }
    public void setOpeningCashBalance(BigDecimal openingCashBalance) { this.openingCashBalance = openingCashBalance; }
    public BigDecimal getOpeningBankBalance() { return openingBankBalance; }
    public void setOpeningBankBalance(BigDecimal openingBankBalance) { this.openingBankBalance = openingBankBalance; }
    public BigDecimal getTotalCashReceipts() { return totalCashReceipts; }
    public void setTotalCashReceipts(BigDecimal totalCashReceipts) { this.totalCashReceipts = totalCashReceipts; }
    public BigDecimal getTotalBankReceipts() { return totalBankReceipts; }
    public void setTotalBankReceipts(BigDecimal totalBankReceipts) { this.totalBankReceipts = totalBankReceipts; }
    public BigDecimal getTotalCashPayments() { return totalCashPayments; }
    public void setTotalCashPayments(BigDecimal totalCashPayments) { this.totalCashPayments = totalCashPayments; }
    public BigDecimal getTotalBankPayments() { return totalBankPayments; }
    public void setTotalBankPayments(BigDecimal totalBankPayments) { this.totalBankPayments = totalBankPayments; }
    public BigDecimal getClosingCashBalance() { return closingCashBalance; }
    public void setClosingCashBalance(BigDecimal closingCashBalance) { this.closingCashBalance = closingCashBalance; }
    public BigDecimal getClosingBankBalance() { return closingBankBalance; }
    public void setClosingBankBalance(BigDecimal closingBankBalance) { this.closingBankBalance = closingBankBalance; }
    public String getDayStatus() { return dayStatus; }
    public void setDayStatus(String dayStatus) { this.dayStatus = dayStatus; }
    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
    public LocalDateTime getClosedTime() { return closedTime; }
    public void setClosedTime(LocalDateTime closedTime) { this.closedTime = closedTime; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

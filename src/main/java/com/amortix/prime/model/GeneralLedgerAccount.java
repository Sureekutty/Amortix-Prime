package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * General Ledger Account Document.
 * Represents Chart of Accounts (Assets, Liabilities, Incomes, Expenses).
 * Corresponds to legacy speccs.SP_Ledger.
 */
@Document(collection = "ledger_accounts")
public class GeneralLedgerAccount {

    @Id
    private String id;

    @Indexed(unique = true)
    private String accountCode; // e.g. 1001, 1002, 2001, 3001, 4001, 5001

    private String accountName; // Cash in Hand, SBI Current A/c, Member Loans Asset, Thrift Deposit Liability
    private String accountType; // ASSET, LIABILITY, EQUITY, INCOME, EXPENSE
    private String normalBalance; // DEBIT, CREDIT

    private BigDecimal currentBalance = BigDecimal.ZERO;
    private BigDecimal openingBalance = BigDecimal.ZERO;

    private boolean active = true;
    private LocalDateTime createdDate = LocalDateTime.now();

    public GeneralLedgerAccount() {}

    public GeneralLedgerAccount(String accountCode, String accountName, String accountType, String normalBalance, BigDecimal openingBalance) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.normalBalance = normalBalance;
        this.openingBalance = openingBalance;
        this.currentBalance = openingBalance;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getNormalBalance() { return normalBalance; }
    public void setNormalBalance(String normalBalance) { this.normalBalance = normalBalance; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

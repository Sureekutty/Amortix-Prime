# Feature: Core Accounting, Double-Entry General Ledger & Daily Cash Book

## Branch Overview
- **Branch Name:** `feature/core-accounting-daybook`
- **Target Integration:** `develop`
- **Module:** Financial Accounting & Ledgers

## Components & Architecture
1. **General Ledger (`GeneralLedgerAccount.java` & `GeneralLedgerAccountRepository.java`):**
   - Chart of Accounts with account groups: Assets, Liabilities, Incomes, Expenditures.
   - Dual-entry credit/debit transaction balance integrity.
2. **Daily Cash Book & Day-End Balancing (`DailyAccountProcessing.java`):**
   - Opening Cash Balance, Inflow Receipts, Outflow Disbursements, Closing Balance.
   - Mathematical balancing invariant verified before day-end book locking:
     $$\text{Closing Cash} = \text{Opening Cash} + \sum \text{Receipts} - \sum \text{Payments}$$
   - Immutable historical day-book snapshots preventing back-dated tampering.
3. **Voucher Management (`Voucher.java` & `VoucherRepository.java`):**
   - Payment Vouchers (Vendor payments, loan disbursements, deposit settlements, administrative expenses).
   - Receipt Vouchers (Loan repayments, thrift collections, share capital fees, miscellaneous receipts).
4. **Web Controller (`AccountingController.java`):**
   - Routing: `/accounting/daybook`, `/accounting/vouchers`, `/accounting/vouchers/new`, `/accounting/trial-balance`.
5. **UI Templates:**
   - `templates/accounting/daybook.html`: Daily cash book register with balancing ledger and lock status.
   - `templates/accounting/vouchers.html`: Voucher search, filtering, and audit inspection.
   - `templates/accounting/voucher_form.html`: Interactive voucher generation with dual-entry ledger mapping.
   - `templates/accounting/trial_balance.html`: Real-time balanced Trial Balance report.

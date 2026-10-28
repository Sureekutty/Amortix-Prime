# Feature: Fixed/Recurring Deposits & Thrift Savings Fund

## Branch Overview
- **Branch Name:** `feature/deposit-thrift-savings`
- **Target Integration:** `develop`
- **Module:** Deposits & Savings

## Components & Architecture
1. **Deposit Products (`DepositAccount.java` & `DepositTransaction.java`):**
   - Fixed Deposits (`FXD`): Lump-sum principal with tenure-based compounding interest.
   - Recurring Deposits (`RCD`): Monthly installment deposits with progressive balance compounding.
   - Premature Encashment: Automatic penal interest recalculation (deducting 1.5% penalty spread).
2. **Financial Math & Compounding Engine (`FinancialMath.java`):**
   - Quarterly compounding adhering strictly to the 3rd-of-the-month cutoff date rule:
     $$A = P \left(1 + \frac{r}{400}\right)^{4t}$$
   - Exact penny rounding compliant with cooperative accounting audits.
3. **Thrift Savings Fund (`ThriftTransaction.java` & `ThriftService.java`):**
   - Mandatory monthly payroll thrift deductions.
   - Annual thrift dividend and compound interest calculation engine with batch ledger posting.
4. **Web Controllers:**
   - `DepositController.java`: `/deposits`, `/deposits/open`, `/deposits/{id}`, `/deposits/{id}/close`.
   - `ThriftController.java`: `/thrift/overview`, `/thrift/member/{id}`, `/thrift/accrue-annual`.
5. **UI Templates:**
   - `templates/deposits/list.html`: Active fixed & recurring deposit portfolio.
   - `templates/deposits/open.html`: New deposit contract generator with maturity schedule preview.
   - `templates/deposits/view.html`: Deposit ledger with accrued interest breakdown.
   - `templates/thrift/overview.html`: Society-wide thrift fund analytics and annual accrual runner.
   - `templates/thrift/member_ledger.html`: Individual member lifetime thrift history.

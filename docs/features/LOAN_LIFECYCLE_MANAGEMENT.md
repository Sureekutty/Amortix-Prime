# Feature: Loan Origination, Amortization & Surety Guarantor Management

## Branch Overview
- **Branch Name:** `feature/loan-lifecycle-management`
- **Target Integration:** `develop`
- **Module:** Loan Portfolio & Repayments

## Components & Architecture
1. **Loan Models:**
   - `Loan.java`: Loan Type (`LTL`, `EXL`, `FDL`), Sanction Amount, Disbursed Amount, Tenure (Months), Interest Rate, EMI, Outstanding Principal, Outstanding Interest, Active Status (`SANCTIONED`, `DISBURSED`, `CLOSED`, `DEFAULTED`).
   - `LoanTransaction.java`: Ledger of monthly repayments partitioned into Principal Component (`P`) and Interest Component (`I`).
   - `SuretyReplacement.java`: Tracks guarantor modifications, outgoing sureties, incoming sureties, and committee approval.
2. **Business Rules & Calculations:**
   - Max loan limits determined by member salary ratio and thrift savings multiplier.
   - Reducing-balance monthly compounding:
     $$\text{Interest} = \frac{\text{Outstanding Principal} \times R}{12 \times 100}$$
   - Mandatory co-member surety guarantors verified against outstanding guarantee caps.
3. **Web Controller (`LoanController.java`):**
   - Routing: `/loans`, `/loans/apply`, `/loans/{id}`, `/loans/{id}/repay`, `/loans/{id}/surety-replace`, `/loans/{id}/statement`.
4. **UI Templates:**
   - `templates/loans/list.html`: Loan directory filtered by active, overdue, and closed facilities.
   - `templates/loans/apply.html`: Loan application form with auto-suggested sureties and live EMI computation.
   - `templates/loans/view.html`: Detailed loan ledger with repayment history and surety roster.
   - `templates/loans/repay.html`: Manual / Over-the-counter loan repayment voucher processor.
   - `templates/loans/surety_replace.html`: Guarantor substitution interface with approval audit.

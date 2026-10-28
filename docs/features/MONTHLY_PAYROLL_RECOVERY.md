# Feature: Monthly Payroll Recovery & Employer Demand Schedule

## Branch Overview
- **Branch Name:** `feature/monthly-payroll-recovery`
- **Target Integration:** `develop`
- **Module:** Payroll Recovery & Demand Processing

## Components & Architecture
1. **Demand Schedule Generation (`MonthlyRecoveryRun.java`):**
   - Scans all active members with active loans and mandatory thrift obligations.
   - Generates aggregate and member-wise demand schedules for employer payroll disbursing officers:
     $$\text{Total Monthly Demand} = \text{Thrift Deduction} + \text{Loan Principal (P)} + \text{Loan Interest (I)}$$
2. **Payroll Response Reconciliation (`MonthlyRecoveryServiceImpl.java`):**
   - Parses incoming employer deduction return files.
   - Reconciles expected demand against actual deducted amount per employee code.
3. **Discrepancy & Variance Handling (`WrongPayment`):**
   - Flags under-deductions (unpaid dues, leave without pay, salary attachments).
   - Flags over-deductions (excess recovery to be credited to member savings/thrift).
   - Generates audit discrepancy log with corrective action workflows.
4. **Web Controller (`RecoveryController.java`):**
   - Routing: `/recovery`, `/recovery/generate`, `/recovery/{id}`, `/recovery/{id}/upload-response`, `/recovery/{id}/export`.
5. **UI Templates:**
   - `templates/recovery/list.html`: History of monthly billing cycles and reconciliation status (`INITIATED`, `GENERATED`, `RECONCILED`, `CLOSED`).
   - `templates/recovery/view.html`: Recovery run inspection with payroll upload dropzone and discrepancy tables.

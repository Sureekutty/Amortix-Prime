# AMORTIX | PRIME — MASTER ARCHITECTURE & ENGINEERING HANDBOOK
## Cooperative Credit & Core Ledger Enterprise Platform
**Author:** Sureendhar J & Architecture Engineering Team  
**Stack:** Java 21 LTS | Spring Boot 3.3 | Spring Data MongoDB | Spring Security 6 | Pure CSS3 (Zero-JS Monolith)

---

## 1. FUNDAMENTALS EXPLAINED FROM ABSOLUTE SCRATCH

### 1.1 What is HTML?
* **HTML stands for:** **HyperText Markup Language**.
* **Definition:** HTML is the standard markup language used to structure web pages and their content. It consists of a series of elements represented by "tags" (such as `<header>`, `<main>`, `<form>`, `<table>`, `<input>`) which tell the web browser how to display text, images, forms, and data.
* **Why HTML5 Semantic Elements Matter in AMORTIX | PRIME:**
  Instead of generic `<div>` tags everywhere, this platform uses semantic tags like `<aside>`, `<nav>`, `<header>`, `<main>`, and `<table>`. Semantic HTML improves accessibility, browser performance, and enables clean document tree parsing.

### 1.2 What is CSS?
* **CSS stands for:** **Cascading Style Sheets**.
* **Definition:** CSS is a style sheet language used for describing the presentation, visual aesthetics, layout, and responsiveness of a document written in HTML.
* **The "Zero-JavaScript" Philosophy in AMORTIX | PRIME:**
  Modern web development often introduces fragile JavaScript libraries for trivial interactions like modals, dropdowns, and tabs. In this project, **ZERO JavaScript is used on the frontend**. 
  * Modals are powered by the pure CSS `:target` pseudo-class (e.g. `<a href="#disburseModal">` activates `#disburseModal:target { display: flex; }`).
  * Collapsibles use native HTML5 `<details>` and `<summary>`.
  * Tabs use CSS radio selector chaining (`input[type="radio"]:checked + label`).
  * Forms use native server-side validation with standard HTTP POST submissions.

### 1.3 What is Java & Java 21 LTS?
* **Java:** A high-level, class-based, object-oriented programming language designed to have as few implementation dependencies as possible ("Write Once, Run Anywhere").
* **Java 21 (Long-Term Support):**
  * **Records:** Compact, immutable data carriers used for calculation results and data transfer objects (DTOs).
  * **Virtual Threads (Project Loom):** High-throughput, lightweight threads allowing high concurrency with minimal operating system thread overhead.
  * **High-Precision BigDecimal:** All monetary transactions, interest rates, and loan balances use `java.math.BigDecimal` with `RoundingMode.HALF_UP` to prevent floating-point rounding errors.

### 1.4 What is Spring Boot 3?
* **Spring Boot:** An open-source, micro-framework that makes it easy to create stand-alone, production-grade Spring-based Applications with embedded Tomcat.
* **Key Components Used:**
  * **Spring MVC:** Model-View-Controller framework handling server-side rendering of HTML pages using Thymeleaf template engine.
  * **Spring Data MongoDB:** Provides repository abstractions (`MongoRepository`), object mapping (`@Document`, `@Field`, `@Id`), and dynamic query generation for MongoDB.
  * **Spring Security 6:** Enforces role-based authorization (`ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_MEMBER`), CSRF protection, and session security.

### 1.5 What is MongoDB & Why NoSQL for AMORTIX | PRIME?
* **MongoDB:** A document-oriented NoSQL database that stores data in flexible, JSON-like BSON (Binary JSON) documents.
* **Why MongoDB Replaces Relational MySQL/Sybase in This Platform:**
  1. **Rich Embedded Value Objects:** Cooperative entities like a `Member` contain nested addresses, nominee lists, and bank account information. In SQL, this requires 4-5 join tables (`speccs.MemAddress`, `speccs.Nominee`, `speccs.NomineeRef`, `speccs.MemberBank`). In MongoDB, these are stored naturally as embedded documents inside a single `members` document.
  2. **High-Speed Atomic Updates:** Member balances, cumulative thrift pools, and loan ledger entries can be queried and updated atomically without foreign key lock contention.
  3. **Cloud Native Portability:** Connects effortlessly to cloud database clusters (such as MongoDB Atlas) with zero schema migration friction.

---

## 2. LEGACY SYSTEM (`Societyold` / `SocietyNew`) AUDIT & MIGRATION MATRIX

The legacy system was built using Sybase / Microsoft SQL Server with raw JDBC and JSP servlets under the `speccs` database schema. Earlier versions of LMS skipped many core banking rules. AMORTIX | PRIME restores and modernizes **every single module with zero tolerance for skipped features**.

### 2.1 Complete 47-Table Legacy Schema Map
| # | Legacy Table | Original Purpose | AMORTIX | PRIME Modern Equivalent |
|---|---|---|---|
| 1 | `speccs.AutoNumbers` | Document sequence counter | Spring Data Sequence Generator & Automated Prefixes |
| 2 | `speccs.BankBalance` | Bank balance ledger | `DailyAccountProcessing` (Bank Position) |
| 3 | `speccs.BankTransactions` | Bank journal entries | `Voucher` & `DailyAccountProcessing` |
| 4 | `speccs.Bills` | Bill payments | `Voucher` (PAYMENT / MISC_EXPENSE) |
| 5 | `speccs.DailyAccountsProcessing` | Day book balancing & closing | `DailyAccountProcessing` & `AccountingService` |
| 6 | `speccs.Deposits` | Term & Fixed deposits | `DepositAccount` (FXD, RCD, MIS) |
| 7 | `speccs.DepositTransactions` | Deposit ledger transactions | `DepositTransaction` |
| 8 | `speccs.EmployeeData` | Staff credentials | `UserAccount` (Spring Security) |
| 9 | `speccs.Interest` | Dynamic interest rate table | `InterestRateRule` Master Engine |
| 10 | `speccs.Loans` | Member loan accounts | `Loan` Document (LTL, EXL, ELG, FDL) |
| 11 | `speccs.Loanstatus` | Sanction & approval tracking | `Loan.status` workflow states |
| 12 | `speccs.LoanTransactions` | Loan repayment ledger | `LoanTransaction` |
| 13 | `speccs.Login` | Authentication records | `UserAccount` (BCrypt encoded) |
| 14 | `speccs.MemAddress` | Member physical addresses | `Member.Address` embedded list |
| 15 | `speccs.MemberAccount` | Member co-op accounts | `Member` Document Core |
| 16 | `speccs.MemberBank` | Member disbursement bank details | `Member.BankDetail` embedded object |
| 17 | `speccs.Members` | Member master data | `Member` Document Core |
| 18 | `speccs.MembersBasic` | Basic pay & salary particulars | `Member.basicPay`, `grossPay`, `netPay` |
| 19 | `speccs.MiscellaneousPayment` | Office administrative payments | `Voucher` (MISC_EXPENSE) |
| 20 | `speccs.MisPayments` | Vendor bills & vouchers | `Voucher` (PAYMENT) |
| 21 | `speccs.MisPaymentsdummy` | Temporary payment staging | Handled in-memory / Transactional |
| 22 | `speccs.MonthlyInstallModify` | Loan installment changes | `Loan.monthlyInstallment` updates |
| 23 | `speccs.MonthlyInstallModifyThrift` | Thrift subscription changes | `Member.monthlyThriftAmount` updates |
| 24 | `speccs.MonthlyProcess` | Monthly demand schedule generation | `MonthlyRecoveryRun` Engine |
| 25 | `speccs.MonthlyResponse` | Payroll response file import | `MonthlyRecoveryRun.reconcilePayrollResponse` |
| 26 | `speccs.Nominee` | Nominee registration | `Member.Nominee` embedded list |
| 27 | `speccs.NomineeRef` | Nominee age & guardian | `Member.Nominee.guardianName` |
| 28 | `speccs.Payments` | Outflow vouchers | `Voucher` (PAYMENT) |
| 29 | `speccs.PayVouchers` | Payment vouchers register | `VoucherRepository` |
| 30 | `speccs.ProcessMonth` | Active billing month tracker | `MonthlyRecoveryRun.month`, `year` |
| 31 | `speccs.ProcessStatus` | Monthly recovery status | `MonthlyRecoveryRun.status` |
| 32 | `speccs.Receipts` | Inflow receipts register | `Voucher` (RECEIPT) |
| 33 | `speccs.Relations` | Nominee relationship master | Validated Enum / String constants |
| 34 | `speccs.Rules` | Co-op society business rules | `SocietyRule` Master Engine |
| 35 | `speccs.Shares` | Member share capital register | `Member.shareCapitalBalance`, `totalShares` |
| 36 | `speccs.SP_RDUploads` | Recurring deposit uploads | `DepositService.recordRcdInstallment` |
| 37 | `speccs.Staff` | Society staff & officers | `UserAccount` with `ROLE_STAFF` |
| 38 | `speccs.Surety` | Guarantor linkages | `Loan.sureties` & `SuretyReplacement` |
| 39 | `speccs.TBSC_EVENTS` | Audit event logging | `AuditLog` Document & Service |
| 40 | `speccs.TBSC_EXCEPTIONLOG` | Error & exception logs | `GlobalExceptionHandler` & `AuditLog` |
| 41 | `speccs.ThriftInterestAmnt` | Annual thrift interest records | `ThriftTransaction` (INTEREST_CREDIT) |
| 42 | `speccs.ThriftInterestUploading` | Staging for thrift interest | `ThriftService.calculateAndCreditAnnualThriftInterest` |
| 43 | `speccs.ThriftLoanUpdateStatus` | Thrift deduction on loans | Integrated in Loan Sanction |
| 44 | `speccs.ThriftTransactions` | Thrift ledger transactions | `ThriftTransaction` |
| 45 | `speccs.TransactionLog` | System audit trail | `AuditLog` |
| 46 | `speccs.TransactionType` | Transaction codes | Transaction Enum types |
| 47 | `speccs.WrongPayment` | Under/over deduction variances | `MonthlyRecoveryRun.WrongPaymentRecord` |

---

## 3. ARCHITECTURE & SYSTEM WORKFLOWS

### 3.1 High-Level Monolithic Architecture
```
+-------------------------------------------------------------------------------+
|                             CLIENT WEB BROWSER                                |
|  Pure HTML5 Semantic Views + CSS3 Tokens (Zero JavaScript / No Client State)  |
+-------------------------------------------------------------------------------+
                                      |
                      Standard HTTP GET / POST Requests
                      (CSRF Token Encrypted in Form Headers)
                                      |
+-------------------------------------v-----------------------------------------+
|                  AMORTIX | PRIME MONOLITHIC SERVER (PORT 8080)               |
|                                                                               |
|  [Spring Security 6] -> Form-Based Session Auth (BCrypt / RBAC)               |
|  [Spring MVC Web]     -> Thymeleaf Template Engine + Server Flash Attributes   |
|  [Service Layer]      -> Business Logic, Financial Math, Accounting Ledgers   |
|  [Spring Data Mongo]  -> Document Repositories & Audit Event Listeners         |
+-------------------------------------------------------------------------------+
                                      |
                      MongoDB Wire Protocol (Port 27017)
                                      |
+-------------------------------------v-----------------------------------------+
|                         MONGODB DATABASE ENGINE                               |
|  Database: amortix_prime_db                                                   |
|  Collections: members, loans, deposit_accounts, vouchers, recovery_runs...    |
+-------------------------------------------------------------------------------+
```

### 3.2 Key Functional Workflows

#### Workflow A: Member Onboarding & Share Allotment
1. Operator navigates to `/members/new` and enters member personal details, employee ID, basic pay, and bank particulars.
2. System allocates an automated member code (`MEM-1001`).
3. Share capital (e.g. 10 shares @ ₹100 = ₹1,000) and initial thrift subscription (e.g. ₹1,000/month) are recorded.
4. Member profile is active and eligible for credit and deposit facilities.

#### Workflow B: Loan Sanction & Disbursement
1. Borrower submits loan application at `/loans/apply` for product:
   * **LTL** (Long Term Loan, 9.0%)
   * **EXL** (Executive / Express Loan, 10.5%)
   * **ELG** (Emergency Loan, 8.5%)
   * **FDL** (Fixed Deposit Loan against collateral, 8.0%)
2. Server validates business rules:
   * `MAX_LOAN_MULTIPLIER`: Requested amount must not exceed 10x member's Basic Pay.
   * `MAX_SURETIES_PER_MEMBER`: Each guarantor must have guaranteed $\le 3$ active loans.
3. Reducing balance monthly installment (EMI) is calculated using `FinancialMath`.
4. Officer approves loan (`/loans/approve/{loanNo}`).
5. Officer disburses funds (`/loans/disburse/{loanNo}`). System automatically:
   * Updates loan status to `ACTIVE`.
   * Posts initial disbursement to `loan_transactions`.
   * Increments active surety counts for all guarantors.
   * Generates official Payment Voucher debiting Loan Asset and crediting Bank/Cash.
   * Generates downloadable official PDF Sanction Order.

#### Workflow C: Monthly Recovery Demand & Payroll Reconciliation
1. At the beginning of the month, officer clicks **Generate Demand Schedule** (`/recovery/generate`).
2. System iterates through all active members and compiles:
   * Monthly Compulsory Thrift subscription.
   * Active Loan Principal portion and Reducing Interest.
   * Active Recurring Deposit (RCD) installment.
3. System outputs a formatted text/CSV Demand Schedule (`Demand_Schedule_REC-2026-10.txt`) for the employer payroll department.
4. When payroll returns the deduction file, the officer uploads it at `/recovery/reconcile/{runCode}`.
5. The Reconciliation Engine executes the statutory waterfall allocation:
   * **Full Match:** Automatically credits Thrift ledger, updates Loan Principal and Interest balances, and posts vouchers.
   * **Short Payment:** Priority: Thrift $\rightarrow$ Interest $\rightarrow$ Principal. Any unrecovered balance is logged into `WrongPayment` discrepancy register for recovery in subsequent cycles.
   * **Excess Payment:** Surplus is credited to the member's Thrift pool.

#### Workflow D: Day Book Balancing & Daily Accounts Closing
1. Every morning, the system establishes a new `DailyAccountProcessing` record pulling opening cash and bank balances from yesterday's closing.
2. Every voucher (Receipt, Payment, Journal Voucher) dynamically shifts cash or bank tallies.
3. At day end, the cashier navigates to `/accounting/daybook` and inspects:
   $$\text{Closing Cash} = \text{Opening Cash} + \text{Total Cash Receipts} - \text{Total Cash Disbursements}$$
4. Cashier confirms denomination verification and locks the day book. No retrospective edits can be made to locked dates.

---

## 4. MATHEMATICAL FORMULAS & PRECISION RULES

### 4.1 Reducing Balance Loan EMI
$$E = \frac{P \cdot r \cdot (1 + r)^n}{(1 + r)^n - 1}$$
Where:
* $P$ = Principal Loan Sanction Amount
* $r$ = Monthly interest rate ($\frac{\text{Annual Rate}}{12 \times 100}$)
* $n$ = Tenure in months

### 4.2 Fixed Deposit Quarterly Compounding (Legacy SP_Calculations)
$$A = P \left(1 + \frac{r}{400}\right)^{4 \times \frac{n}{12}}$$
Where:
* $A$ = Maturity Value
* $P$ = Principal Deposit Amount
* $r$ = Annual Interest Rate percentage
* $n$ = Tenure in months

### 4.3 Legacy Cutoff Rule for Deposit Maturity Date
$$\text{Maturity Date} = \begin{cases} 
\text{Deposit Date} + n \text{ Months} & \text{if } \text{Day} \le 3 \\
\text{First Day of } (\text{Month} + n + 1) & \text{if } \text{Day} > 3 
\end{cases}$$

### 4.4 Premature Deposit Closure Penalty
$$\text{Effective Rate} = \max(0.5, \text{Contract Rate} - \text{Penalty Rate (1.5\%)}) \quad (\text{Calculated via Simple Interest})$$

---

## 5. SECURITY & ROLE-BASED ACCESS CONTROL (RBAC)

Spring Security 6 enforces strict server-side authorization:
* `ROLE_ADMIN`: Full governance access. Can edit master rules, modify product interest rates, conduct AGM ballots, view immutable audit logs, and execute full member settlements.
* `ROLE_STAFF`: Credit and accounting operations. Can register members, process loan applications, approve/disburse loans, open term deposits, generate monthly recovery schedules, and balance the day book.
* `ROLE_MEMBER`: Self-service view. Can view personal 360 portfolio, loan ledgers, thrift passbooks, and vote on open AGM resolutions.

---

## 6. CLOUD HOSTING & PRODUCTION DEPLOYMENT GUIDE

AMORTIX | PRIME is production-ready for deployment to Render, Railway, Docker, or Cloud Virtual Machines (like Land Management).

### 6.1 Online Deployment via Render.com (1-Click Blueprint)
1. Push this repository to GitHub: `https://github.com/Sureekutty/Amortix-Prime`.
2. Log in to [Render Dashboard](https://dashboard.render.com).
3. Click **New +** $\rightarrow$ **Blueprint** and connect the `Sureekutty/Amortix-Prime` repository.
4. Render will read `render.yaml` and configure the web service using the multi-stage `Dockerfile`.
5. Under **Environment Variables**, configure:
   * `MONGODB_URI`: Your MongoDB Atlas connection string (e.g. `mongodb+srv://user:pass@cluster.mongodb.net/amortix_prime_db?retryWrites=true&w=majority`).
   * `PORT`: `8080`
   * `SPRING_PROFILES_ACTIVE`: `prod`
6. Click **Deploy**. The platform will compile with Maven and launch the secure Spring Boot container.

### 6.2 Running Locally with Docker Compose
```bash
# Clone the repository
git clone https://github.com/Sureekutty/Amortix-Prime.git
cd Amortix-Prime

# Launch both MongoDB and Spring Boot Monolith
docker-compose up --build
```
Access the application at `http://localhost:8080`.

### 6.3 Running Locally with Native Java & Maven
Ensure local MongoDB is running on port 27017:
```bash
./mvnw clean spring-boot:run
```
Login credentials:
* **Admin:** `admin` / `admin123`
* **Staff:** `staff` / `staff123`
* **Member:** `member` / `member123`

---

## 7. VERIFICATION & TEST EXECUTION

Run the automated test suite verifying financial math, recovery calculations, and day book balancing:
```bash
./mvnw test
```
**Test Coverage Includes:**
* `FinancialMathTest`: Reducing balance EMI calculations, quarterly compound interest, and legacy 3rd-of-month date cutoff validation.
* `MonthlyRecoveryEngineTest`: Demand schedule compilation and shortage variance discrepancy detection.
* `DayBookBalancingTest`: Mathematical verification of vault cash inflows, outflows, and day-end lock balances.

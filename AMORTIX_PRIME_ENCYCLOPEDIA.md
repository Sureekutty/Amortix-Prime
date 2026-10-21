# AMORTIX | PRIME — THE COMPLETE COOPERATIVE CORE BANKING ENCYCLOPEDIA
## The Comprehensive Master Engineering & Interview Preparation Guide
**Project:** AMORTIX | PRIME  
**Repository:** [https://github.com/Sureekutty/Amortix-Prime](https://github.com/Sureekutty/Amortix-Prime)  
**Author:** Sureendhar J & Core Architecture Team  
**System Architecture:** Enterprise Server-Side Monolith | Zero-JavaScript Frontend | Java 21 LTS | Spring Boot 3.3 | Spring Data MongoDB | Spring Security 6  

---

## TABLE OF CONTENTS
1. [Foundational Technologies from Absolute Scratch](#1-foundational-technologies-from-absolute-scratch)
   - 1.1 What is HTML? (History, DOM, Every Tag Used)
   - 1.2 What is CSS? (Box Model, Flexbox, Grid, Custom Properties, Zero-JS Techniques)
   - 1.3 What is Java? (OOP, Java 21 LTS Features, BigDecimal Banking Math)
   - 1.4 What is Spring Boot? (IoC, DI, DispatcherServlet, Annotations Lifecycle)
   - 1.5 What is Spring Security? (Authentication vs Authorization, CSRF, BCrypt, Filter Chains)
   - 1.6 What is MongoDB? (NoSQL Document Model, BSON, Collections vs Tables, Indexes, ACID)
2. [Architectural Philosophy: Why Monolith + Zero-JS SSR?](#2-architectural-philosophy-why-monolith--zero-js-ssr)
3. [The Legacy System (`Societyold` / `SocietyNew`) Deep Audit](#3-the-legacy-system-societyold--societynew-deep-audit)
   - 3.1 Complete 47-Table Legacy Schema Breakdown
   - 3.2 Legacy Stored Procedures & Business Logic Decoded
   - 3.3 Comprehensive Legacy vs Modern Comparison Matrix
4. [End-to-End System Workflows & Data Flow Diagrams](#4-end-to-end-system-workflows--data-flow-diagrams)
5. [Exhaustive Functional Modules Breakdown](#5-exhaustive-functional-modules-breakdown)
   - 5.1 Member 360° Management & Full Retirement Settlement
   - 5.2 Share Capital Allotment & Tracking
   - 5.3 Compulsory Monthly Thrift Fund & Annual Interest Batch Run
   - 5.4 Term & Recurring Deposits (FXD, RCD, MIS & 3rd-of-Month Rule)
   - 5.5 Credit & Lending Suite (LTL, EXL, ELG, FDL & Surety Replacement)
   - 5.6 Monthly Recovery & Payroll Reconciliation Engine (Waterfall Priority)
   - 5.7 Double-Entry Treasury Day Book & Cash Vault Balancing
   - 5.8 Dynamic Governance Rules & Immutable Audit Trail
6. [Mathematical Formulas & Financial Algorithms](#6-mathematical-formulas--financial-algorithms)
7. [Data Dictionary & MongoDB Document Schemas](#7-data-dictionary--mongodb-document-schemas)
8. [Testing Suite, Edge Cases & Verification](#8-testing-suite-edge-cases--verification)
9. [Production Cloud Hosting & Deployment Manual](#9-production-cloud-hosting--deployment-manual)
10. [Senior Software Engineer Interview Masterclass (Q&A)](#10-senior-software-engineer-interview-masterclass-qa)

---

# 1. FOUNDATIONAL TECHNOLOGIES FROM ABSOLUTE SCRATCH

---

### 1.1 What is HTML?

#### A. Definition & Expansion
* **HTML stands for:** **HyperText Markup Language**.
  * **HyperText:** Text that links to other pieces of text or resources across the web (hyperlinks).
  * **Markup:** A system of annotating text using syntactic markers ("tags") that instruct a parser how to structure, format, and display the content.
  * **Language:** A standardized set of rules and syntax understood by web browsers globally (standardized by the W3C and WHATWG).

#### B. How the Browser Processes HTML
1. **Bytes to Characters:** The web browser requests an HTML document over HTTP/HTTPS. It receives raw binary bytes (e.g., `3C 68 74 6D 6C 3E`).
2. **Tokenization:** The parser reads the bytes according to character encoding (UTF-8) and converts them into tokens (e.g., `StartTag: html`, `StartTag: body`, `EndTag: html`).
3. **Node Creation:** Tokens are converted into objects with properties and rules called **Nodes**.
4. **DOM Construction:** The browser links these nodes into a hierarchical tree data structure known as the **DOM (Document Object Model)**.

#### C. Every HTML Tag Used in AMORTIX | PRIME & Why
* `<!DOCTYPE html>`: Declares that the document adheres to the **HTML5** standard, preventing browsers from rendering in outdated "quirks mode".
* `<html>`: The root container element enclosing all web content.
* `<head>`: Contains machine-readable metadata not directly visible to the user:
  * `<meta charset="UTF-8">`: Specifies the Unicode character encoding to prevent garbled text.
  * `<meta name="viewport" content="width=device-width, initial-scale=1.0">`: Controls the viewport scaling for responsive displays on mobile, tablet, and desktop monitors.
  * `<title>`: Defines the document title shown on browser tabs and bookmarks.
  * `<link rel="stylesheet">`: Links external CSS style sheets to the document.
* `<body>`: The container for all visible content rendered to the user.
* `<aside>`: Represents a sidebar containing navigational links separate from the primary page content.
* `<header>`: Represents introductory content or top navigation bars.
* `<main>`: Encloses the dominant, unique content of the page body.
* `<h1>`, `<h3>`, `<h4>`: Structural heading hierarchy giving semantic weight to titles.
* `<nav>` & `<ul>`, `<li>`: Unordered lists used for semantic menu navigation.
* `<a>`: Anchor element creating hyperlinks (`href="/loans"`, `href="#disburseModal"`).
* `<form>`: Interactive container for collecting user input and submitting HTTP POST/GET requests to the server.
  * `method="post"`: Sends data securely inside the HTTP request body rather than query parameters.
  * `action="@{/loans/save}"`: Specifies the target Spring MVC controller URL.
  * `enctype="multipart/form-data"`: Used in file upload forms (e.g. uploading monthly payroll response files) to transmit binary streams.
* `<label>`: Associates text with form inputs using the `for` attribute for accessibility and screen readers.
* `<input>`: Flexible interactive control:
  * `type="text"`: Single-line text input.
  * `type="number"`: Numeric input supporting decimals (`step="0.01"` for currency amounts).
  * `type="date"`: Native calendar date picker returning ISO-8601 strings (`YYYY-MM-DD`).
  * `type="password"`: Masked text field obscuring input.
  * `type="radio"`: Mutually exclusive option selection (used in AGM voting and pure CSS tabs).
  * `type="hidden"`: Non-visible fields carrying security tokens (CSRF) or document identifiers.
* `<select>` & `<option>`: Dropdown menu providing fixed choices (Loan types, compounding frequencies).
* `<datalist>`: Provides autocomplete suggestions for inputs without restricting users to fixed dropdowns (used for searching Member Codes while typing).
* `<textarea>`: Multi-line text field for entering long remarks, loan purposes, and voucher narrations.
* `<button>`: Clickable control (`type="submit"` initiates form validation and submission).
* `<table>`, `<thead>`, `<tbody>`, `<tr>`, `<th>`, `<td>`: Semantic tabular data elements used for high-density ledgers, cash books, and member directories.
* `<div>` & `<span>`: Generic block-level and inline containers used for layout styling and badge indicators.

---

### 1.2 What is CSS?

#### A. Definition & Expansion
* **CSS stands for:** **Cascading Style Sheets**.
  * **Cascading:** The algorithm that resolves conflicts when multiple style rules match an element based on importance, origin, specificity, and source order.
  * **Style:** Visual aesthetics including typography, colors, borders, shadows, margins, and padding.
  * **Sheets:** Separate documents containing style rules, decoupling content structure from presentation.

#### B. The CSS Box Model
Every element in CSS is treated as a rectangular box composed of four concentric layers:
1. **Content:** The actual text, image, or child element (`width` and `height`).
2. **Padding:** Transparent space surrounding the content inside the border.
3. **Border:** A visible or invisible line enclosing the padding and content.
4. **Margin:** Transparent space outside the border separating the element from neighboring elements.
* **Why `box-sizing: border-box;` is mandatory in AMORTIX | PRIME:**
  By default (`content-box`), adding padding expands the total width of an element, causing layouts to break. With `box-sizing: border-box;`, padding and borders are included *inside* the defined width, preventing calculation errors.

#### C. Modern Layout Systems: Flexbox & CSS Grid
* **CSS Grid (`display: grid`):** A two-dimensional layout system managing both rows and columns simultaneously.
  * *Used in AMORTIX | PRIME for:* Stat card grids (`grid-template-columns: repeat(auto-fit, minmax(220px, 1fr))`) and multi-column banking form grids (`.form-grid`).
* **Flexbox (`display: flex`):** A one-dimensional layout system managing alignment along a primary axis.
  * *Used in AMORTIX | PRIME for:* Top navigation bars, button groups, badge alignments, and table cell formatting.

#### D. CSS Custom Properties (Design Tokens)
AMORTIX | PRIME uses CSS variables declared on `:root` to ensure system-wide visual consistency:
```css
:root {
  --bg-main: #0b0f19;       /* Deep Obsidian Core */
  --bg-sidebar: #0f172a;    /* Navy Sidebar */
  --bg-card: #1e293b;       /* Elevated Slate Card */
  --primary: #38bdf8;       /* Sapphire Banking Accent */
  --success: #10b981;       /* Emerald Vault Inflow */
  --danger: #ef4444;        /* Ruby Red Outflow/Warning */
  --border-color: #334155;  /* Subtle Boundary */
}
```

#### E. Pure CSS Zero-JavaScript Interactivity
1. **Modals via CSS `:target`:**
   * An anchor link points to a fragment identifier: `<a href="#disburseModal">Disburse</a>`.
   * When clicked, the browser URL updates to `...#disburseModal`.
   * CSS activates the overlay with zero JavaScript:
     ```css
     .modal-overlay { display: none; }
     .modal-overlay:target { display: flex; }
     ```
   * Clicking the close button (`<a href="#" class="modal-close">&times;</a>`) clears the target hash, closing the modal instantly.
2. **Interactive States:**
   * `:hover`: Elevates cards (`transform: translateY(-2px);`) and highlights table rows.
   * `:focus`: Highlights active inputs with `--primary` borders.
   * `:checked`: Toggles pure CSS radio tabs and ballot selections.

---

### 1.3 What is Java & Java 21 LTS?

#### A. Definition & Core Principles
* **Java:** A strongly typed, compiled, class-based object-oriented language running on the Java Virtual Machine (JVM).
* **Core Principles:**
  * **Encapsulation:** Keeping fields private and exposing controlled operations via getters, setters, and business methods.
  * **Inheritance:** Reusing code across hierarchies (e.g., Spring Data repositories extending `MongoRepository`).
  * **Polymorphism:** Handling distinct loan types (`LTL`, `EXL`, `ELG`, `FDL`) through uniform interfaces.
  * **Abstraction:** Hiding complex persistence and transaction logic behind clean service layer interfaces (`LoanService`, `AccountingService`).

#### B. Java 21 Long-Term Support (LTS) Features Used
1. **Records:** Compact, immutable classes used for calculation results:
   ```java
   public record DayBookSummary(
       LocalDate date,
       BigDecimal openingCash,
       BigDecimal closingCash,
       List<Voucher> vouchers
   ) {}
   ```
2. **Virtual Threads (Project Loom):** High-throughput, lightweight threads enabling the server to process thousands of concurrent banking requests without thread exhaustion.
3. **Sequenced Collections:** Uniform access to first and last elements in ledger transactions.

#### C. Why `BigDecimal` is Mandatory in Financial Engineering
In Java, primitive floating-point types (`float` and `double`) use binary IEEE 754 representations. They cannot represent decimal fractions like `0.1` precisely:
```java
// DANGEROUS FLOAT BUG:
double val = 0.1 + 0.2; // Results in 0.30000000000000004!
```
In banking and loan amortization, even a $0.00001$ discrepancy causes double-entry ledgers to fail balancing audits. Therefore, **AMORTIX | PRIME exclusively uses `java.math.BigDecimal`** with `RoundingMode.HALF_UP` for all interest, principal, EMI, and ledger balances.

---

### 1.4 What is Spring Boot 3?

#### A. Core Concepts
* **Spring Boot:** An opinionated framework that simplifies building enterprise Spring applications by providing pre-configured starters, auto-configuration, and an embedded web server (Tomcat).
* **Inversion of Control (IoC):** The framework controls the lifecycle and execution flow of application objects rather than custom application code.
* **Dependency Injection (DI):** Objects receive their dependencies from the Spring IoC Container rather than constructing them via `new`. In AMORTIX | PRIME, all dependencies are injected via clean **Constructor Injection**:
  ```java
  @Service
  public class LoanServiceImpl implements LoanService {
      private final LoanRepository loanRepository;
      public LoanServiceImpl(LoanRepository loanRepository) {
          this.loanRepository = loanRepository;
      }
  }
  ```

#### B. The Spring MVC Request Lifecycle
When a user submits a form or accesses a URL:
1. **HTTP Request** arrives at the server on port `8080`.
2. **`DispatcherServlet`** (the Front Controller) intercepts the request.
3. **`HandlerMapping`** matches the URL to a specific Controller method (e.g. `@GetMapping("/loans")`).
4. **Spring Security Filter Chain** authenticates the session and verifies user permissions.
5. **Controller Method** executes, calls the Service Layer, populates the `Model` map with data attributes, and returns a view name string (e.g. `"loans/list"`).
6. **`ViewResolver`** resolves `"loans/list"` to the Thymeleaf template `templates/loans/list.html`.
7. **Thymeleaf Engine** merges the model data with the HTML template on the server, generating pure HTML5.
8. **HTTP Response** containing the finished HTML is streamed back to Chrome.

#### C. Key Spring Annotations Used
* `@SpringBootApplication`: Marks the main launch class, enabling auto-configuration, component scanning, and property support.
* `@Controller`: Designates a Spring MVC web controller returning rendered HTML views.
* `@Service`: Marks business logic components containing core banking calculations and transactional workflows.
* `@Repository`: Designates data access components interacting with MongoDB.
* `@Document`: Maps a Java class to a MongoDB collection.
* `@Transactional`: Wraps a method in an atomic database transaction. If an exception occurs, all database writes are rolled back.
* `@GetMapping` / `@PostMapping`: Maps HTTP GET and POST requests to controller handler methods.
* `@ModelAttribute`: Binds form inputs directly to Java objects.
* `@PathVariable`: Extracts dynamic segments from URLs (e.g. `/members/view/{memberCode}`).
* `@RequestParam`: Extracts HTTP query parameters (e.g. `?search=MEM-1001&page=0`).

---

### 1.5 What is Spring Security 6?

#### A. Authentication vs. Authorization
* **Authentication ("Who are you?"):** Verifying user identity. In AMORTIX | PRIME, this is handled via form login matching credentials against `UserAccount` documents.
* **Authorization ("What are you allowed to do?"):** Granting access to resources based on assigned roles:
  * `ROLE_ADMIN`: Access to governance rules, interest rate configuration, AGM polls, and full settlements.
  * `ROLE_STAFF`: Access to loans, deposits, member onboarding, recovery schedules, and day book balancing.
  * `ROLE_MEMBER`: Restricted to viewing personal portfolio, loans, deposits, and voting.

#### B. BCrypt Password Hashing
Passwords are never stored in plaintext. AMORTIX | PRIME uses `BCryptPasswordEncoder`:
* BCrypt incorporates a **128-bit cryptographically secure salt** to protect against rainbow table attacks.
* It uses an adaptive work factor (iteration count) that can be increased as hardware advances, making brute-force attacks computationally infeasible.

#### C. Cross-Site Request Forgery (CSRF) Protection
* **What is CSRF?** An attack where a malicious website tricks a logged-in user's browser into executing unwanted actions on a trusted site.
* **How Spring Security Prevents It:**
  Spring Security generates a unique, cryptographically random CSRF token tied to the user's session. Whenever a form is submitted via POST, Thymeleaf automatically embeds this token as a hidden field (`<input type="hidden" name="_csrf" value="...">`). The server validates the token before processing any database mutation. If the token is missing or invalid, the request is rejected with `HTTP 403 Forbidden`.

---

### 1.6 What is MongoDB & Why NoSQL?

#### A. Document-Oriented Model (BSON)
Unlike relational databases that store data in rigid rows and columns with fixed types, MongoDB stores records as **BSON (Binary JSON) documents**. Documents can contain nested sub-documents and arrays:
```json
{
  "_id": "6716a4b1e9c20a32d18f4a12",
  "memberCode": "MEM-1001",
  "fullName": "Sureendhar J",
  "basicPay": 65000.00,
  "addresses": [
    { "addressType": "PRESENT", "city": "Chennai", "pincode": "600002" }
  ],
  "nominees": [
    { "nomineeName": "Vasanthi J", "relationship": "Mother", "sharePercentage": 100.0 }
  ]
}
```

#### B. SQL vs MongoDB Terminology Comparison
| Relational SQL (Sybase / MySQL) | MongoDB NoSQL (AMORTIX | PRIME) |
|---|---|
| Database | Database (`amortix_prime_db`) |
| Table | Collection (`members`, `loans`, `vouchers`) |
| Row / Record | Document |
| Column | Field |
| Primary Key (`PRIMARY KEY`) | Object Identifier (`_id` / `@Id`) |
| Foreign Key (`FOREIGN KEY`) | Embedded Document OR Referenced Identifier |
| SQL `JOIN` | Embedded Value Objects OR Aggregation `$lookup` |

#### C. Why MongoDB Eliminates the N+1 Query Problem in Banking
In relational databases, querying 100 members and their addresses requires:
1. `SELECT * FROM Members` (1 query).
2. For each member: `SELECT * FROM MemAddress WHERE MemCode = ?` (100 separate queries).
This is the classic **N+1 query problem**. In MongoDB, the addresses and nominees are embedded directly within the member document, allowing the entire 360° profile to be fetched in **a single indexed read operation**.

---

# 2. ARCHITECTURAL PHILOSOPHY: WHY MONOLITH + ZERO-JS SSR?

Modern software engineering is plagued by over-engineering: splitting simple applications into microservices, deploying separate React/Vite frontends, managing complex CORS rules, and introducing hundreds of megabytes of `node_modules` dependencies.

AMORTIX | PRIME deliberately rejects this complexity in favor of an **Industrial Monolithic Server-Side Rendered (SSR) Architecture**:

```
+---------------------------------------------------------------------------------------+
|                                 ARCHITECTURAL BENEFITS                                |
+------------------------------------+--------------------------------------------------+
| 1. ZERO JAVASCRIPT SECURITY        | - Immune to client-side XSS DOM injection attacks|
|                                    | - No vulnerable npm dependencies to audit        |
|                                    | - Zero token leakage in browser localStorage     |
+------------------------------------+--------------------------------------------------+
| 2. OPERATIONAL SIMPLICITY          | - Single unified Java runtime                    |
|                                    | - Single HTTP port (8080)                        |
|                                    | - Zero CORS configuration issues                 |
|                                    | - Runs directly in Eclipse with 1 click          |
+------------------------------------+--------------------------------------------------+
| 3. TRANSACTIONAL PURITY            | - Atomic Spring @Transactional boundaries        |
|                                    | - Double-entry ledgers balance synchronously     |
|                                    | - No eventual consistency race conditions        |
+------------------------------------+--------------------------------------------------+
```

---

# 3. THE LEGACY SYSTEM (`Societyold` / `SocietyNew`) DEEP AUDIT

The legacy project was an enterprise Co-operative Thrift & Credit Society software running on Sybase/Microsoft SQL Server with Java Servlets and raw JSP scriptlets.

### 3.1 Complete 47-Table Legacy Schema Map
```
speccs.AutoNumbers                -> Automatic document numbering counters
speccs.BankBalance                -> Bank balance cache
speccs.BankTransactions           -> Bank transaction records
speccs.Bills                      -> Vendor bills
speccs.DailyAccountsProcessing    -> Cash book & day-end lock records
speccs.Deposits                   -> Term & fixed deposit contracts
speccs.DepositTransactions        -> Deposit ledger entries
speccs.EmployeeData               -> Staff profiles
speccs.Interest                   -> Master interest rate tables
speccs.Loans                      -> Loan accounts & sanction details
speccs.Loanstatus                 -> Loan lifecycle status
speccs.LoanTransactions           -> Repayment transaction ledger
speccs.Login                      -> User credentials
speccs.MemAddress                 -> Member addresses
speccs.MemberAccount              -> Member cooperative accounts
speccs.MemberBank                 -> Member bank accounts
speccs.Members                    -> Member master registry
speccs.MembersBasic               -> Salary & basic pay details
speccs.MiscellaneousPayment       -> Office admin expenses
speccs.MisPayments                -> Vendor payment vouchers
speccs.MisPaymentsdummy           -> Staging payments
speccs.MonthlyInstallModify       -> Loan installment adjustments
speccs.MonthlyInstallModifyThrift -> Thrift subscription adjustments
speccs.MonthlyProcess             -> Monthly demand schedule generation
speccs.MonthlyResponse            -> Payroll response import
speccs.Nominee                    -> Member nominees
speccs.NomineeRef                 -> Nominee guardian linkages
speccs.Payments                   -> General payments register
speccs.PayVouchers                -> Payment voucher header
speccs.ProcessMonth               -> Active billing month
speccs.ProcessStatus              -> Billing cycle status
speccs.Receipts                   -> Receipts register
speccs.Relations                  -> Family relation master
speccs.Rules                      -> Society business rules
speccs.Shares                     -> Share capital holdings
speccs.SP_RDUploads               -> Recurring deposit uploads
speccs.Staff                      -> Staff directory
speccs.Surety                     -> Loan guarantors
speccs.TBSC_EVENTS                -> Audit trail
speccs.TBSC_EXCEPTIONLOG          -> System error log
speccs.ThriftInterestAmnt         -> Annual thrift interest credits
speccs.ThriftInterestUploading    -> Staging for thrift interest
speccs.ThriftLoanUpdateStatus     -> Loan recovery status
speccs.ThriftTransactions         -> Thrift fund ledger
speccs.TransactionLog             -> Audit events
speccs.TransactionType            -> Transaction classifications
speccs.WrongPayment               -> Discrepancy & short deduction records
```

### 3.2 Legacy Stored Procedures Decoded
1. **`speccs.SP_Calculations`:**
   * Handled Fixed Deposit (`FXD`) and Recurring Deposit (`RCD`) maturity calculations.
   * Enforced the **3rd-of-the-month cutoff rule**: if deposit date day $\le 3$, maturity date = date + tenure; if day $> 3$, maturity date rolls over to the 1st of the next month plus tenure.
   * Handled quarterly compounding factors ($4 \times \frac{n}{12}$).
   * Applied the `FXP` penalty rate for premature deposit withdrawals.
2. **`speccs.SP_MonthlyProcess`:**
   * Aggregated active member deductions (Loan Principal, Loan Interest, Thrift Subscription, RCD installment) into a monthly demand schedule for employer payroll.
3. **`speccs.SP_MemberSettlementNew`:**
   * Liquidated all cooperative balances upon retirement: refunded Share Capital, Thrift Fund, and active Deposits while deducting outstanding loan principal and interest liabilities.
4. **`speccs.SP_ReceiptsNew` & `speccs.SP_Ledger`:**
   * Maintained double-entry debit and credit balance updates across general ledger accounts.

---

# 4. END-TO-END SYSTEM WORKFLOWS & DATA FLOW DIAGRAMS

### 4.1 Master Operational Flowchart
```
[ Member Enrolls at /members/new ]
               |
               v
[ Allocates Share Capital & Monthly Thrift ]
               |
               +-------------------------------------------------+
               |                                                 |
               v                                                 v
[ Applies for Credit Facility (/loans/apply) ]   [ Books Term Deposit (/deposits/open) ]
  - LTL / EXL / ELG / FDL                          - FXD / RCD / MIS
  - Validates 10x Basic Pay Rule                   - Quarterly Compounding Engine
  - Verifies Guarantor Sureties (<= 3 loans)       - 3rd-of-Month Cutoff Rule
               |                                                 |
               v                                                 v
[ Officer Approves & Disburses Loan ]            [ Active Interest Accrual ]
  - Updates Guarantor Surety Counts                - Premature Penalty Protection
  - Posts Disbursed Voucher & Cash/Bank Outflow                  |
               |                                                 |
               +-----------------------+-------------------------+
                                       |
                                       v
                     [ Monthly Recovery Billing Cycle (/recovery) ]
                       1. Generates Gross Demand Schedule
                       2. Exports Text File for Employer Payroll
                       3. Imports Payroll Deduction Response File
                       4. Waterfall Reconciliation:
                          Thrift -> Loan Interest -> Loan Principal -> RCD
                       5. Logs Shortages to WrongPayment Register
                                       |
                                       v
                     [ Daily Accounts Processing (/accounting/daybook) ]
                       - Records Receipts, Payments & Journal Vouchers (JV)
                       - Tallies Cash Vault Inflows & Outflows
                       - Audits & Locks Day Book
                                       |
                                       v
                     [ Member Full Settlement (/members/settle) ]
                       - Gross Assets (Shares + Thrift + Deposits)
                       - Minus Outstanding Loan Liabilities
                       - Emits Final Settlement Payment Voucher
```

---

# 5. EXHAUSTIVE FUNCTIONAL MODULES BREAKDOWN

### 5.1 Member 360° Management & Settlement Engine
* **Registration:** Enrolls members with personal bio-data, employee codes, basic salary, multiple addresses (Present, Permanent, Office), and nominee percentages.
* **Retirement Settlement (`speccs.SP_MemberSettlementNew`):**
  When a member leaves or retires:
  $$\text{Net Settlement} = (\text{Share Capital} + \text{Thrift Balance} + \text{Active Deposits}) - (\text{Outstanding Loan Principal} + \text{Interest})$$
  The engine atomically closes all active loans, liquidates savings, marks the member `SETTLED`, and emits a formal payment voucher.

### 5.2 Share Capital Allotment
* Members purchase cooperative shares (standard face value ₹100 per share).
* Share capital forms the foundational equity of the cooperative society and earns annual AGM dividends.

### 5.3 Compulsory Monthly Thrift Fund
* Every active member contributes a fixed monthly subscription (e.g., ₹1,000 to ₹3,000) deducted from salary.
* **Annual Batch Interest Engine (`speccs.sp_ThriftInterestUpload`):**
  Once a year, an administrator triggers `/thrift/annual-interest`. The system compounds interest on all active thrift balances, updates member ledgers, and posts a Journal Voucher debiting Society Interest Expense and crediting Member Thrift Liabilities.

### 5.4 Term & Recurring Deposits
* **Fixed Deposit (`FXD`):** Lump-sum deposit with quarterly compounding.
* **Recurring Cumulative Deposit (`RCD`):** Monthly installment savings plan.
* **Monthly Income Scheme (`MIS`):** Monthly interest payout to member bank accounts.
* **Premature Closure Penalty (`FXP`):** Deducts 1.5% from the contracted interest rate and recalculates payout on a simple interest basis.

### 5.5 Credit & Lending Suite
* **Long Term Loan (`LTL`):** Tenures up to 120 months for major capital expenditures.
* **Executive Loan (`EXL`):** Medium-term credit up to 36 months.
* **Emergency Loan (`ELG`):** Short-term rapid assistance up to 12 months.
* **Fixed Deposit Loan (`FDL`):** Secured loan pledged against an active Fixed Deposit certificate (collateral lien).
* **Surety Replacement Workflow (`suretyReplace.jsp`):**
  When a guarantor resigns or retires, a replacement workflow allows swapping guarantors after verifying the new surety has not exceeded their 3-loan guarantee limit.

### 5.6 Monthly Recovery & Payroll Reconciliation Engine
* **Demand Schedule Generation:** Aggregates all expected collections for the month across all members into a single structured demand schedule.
* **Statutory Waterfall Priority Allocation:**
  When employer payroll deductions are uploaded, partial payments are allocated in statutory order:
  1. **Thrift Fund:** Mandatory cooperative savings.
  2. **Loan Interest:** Unpaid interest accrued.
  3. **Loan Principal:** Amortization principal.
  4. **Recurring Deposit:** Savings installment.
* **Discrepancy Logging (`speccs.WrongPayment`):** Any under-deductions are logged into the `WrongPayment` register for audit tracking and arrears recovery.

### 5.7 Double-Entry Treasury Day Book
* **Opening Cash + Inflows - Outflows = Closing Cash.**
* Daily Accounts Processing tallies vault cash and bank balances at the close of business.
* **Day Lock:** Once locked, the date cannot be modified, guaranteeing ledger integrity.
* **Voucher Register:** Supports Receipts, Payments, and Journal Vouchers (`JV`) for internal fund transfers.

---

# 6. MATHEMATICAL FORMULAS & FINANCIAL ALGORITHMS

### 6.1 Reducing Balance Loan EMI
$$E = \frac{P \cdot r \cdot (1 + r)^n}{(1 + r)^n - 1}$$
* **Worked Example:**
  * Principal $P = ₹1,00,000$
  * Annual Rate $= 12\%$ ($\implies$ Monthly Rate $r = \frac{12}{12 \times 100} = 0.01$)
  * Tenure $n = 12\text{ months}$
  * $(1 + 0.01)^{12} = 1.126825$
  * $E = \frac{100000 \times 0.01 \times 1.126825}{1.126825 - 1} = \frac{1126.825}{0.126825} = \mathbf{₹8,884.88}$

### 6.2 Fixed Deposit Quarterly Compounding (Legacy SP_Calculations)
$$A = P \left(1 + \frac{r}{400}\right)^{4 \times \frac{n}{12}}$$
* **Worked Example:**
  * Principal $P = ₹50,000$
  * Annual Rate $r = 8.0\%$
  * Tenure $n = 12\text{ months}$
  * $A = 50000 \left(1 + \frac{8}{400}\right)^4 = 50000 \times (1.02)^4 = 50000 \times 1.082432 = \mathbf{₹54,121.61}$

---

# 7. DATA DICTIONARY & MONGODB DOCUMENT SCHEMAS

### Collection: `members`
* `_id` (ObjectId): Unique MongoDB primary key.
* `memberCode` (String, Indexed, Unique): Business member identifier (e.g. `MEM-1001`).
* `employeeCode` (String, Indexed): Employer payroll employee identifier.
* `fullName` (String): Full legal name of member.
* `basicPay` (BigDecimal): Monthly basic salary used for loan multiplier checks.
* `shareCapitalBalance` (BigDecimal): Accumulated paid-up share capital.
* `totalShares` (Integer): Count of ₹100 shares held.
* `monthlyThriftAmount` (BigDecimal): Compulsory monthly thrift deduction.
* `thriftBalance` (BigDecimal): Accumulated thrift pool balance.
* `activeSuretyCount` (Integer): Number of active loans currently guaranteed ($\le 3$).
* `status` (String): `ACTIVE`, `SETTLED`, `SUSPENDED`.
* `addresses` (Array of Embedded Documents): Address records (`PRESENT`, `PERMANENT`, `OFFICE`).
* `nominees` (Array of Embedded Documents): Nominee details with percentage shares.
* `bankDetail` (Embedded Document): Bank name, account number, IFSC code for electronic disbursements.

### Collection: `loans`
* `_id` (ObjectId): Primary key.
* `loanAccountNo` (String, Indexed, Unique): E.g. `LTL-10001`, `EXL-20001`.
* `memberCode` (String, Indexed): Borrower member code.
* `loanType` (String): `LTL`, `EXL`, `ELG`, `FDL`.
* `sanctionAmount` (BigDecimal): Approved loan principal.
* `interestRate` (Double): Annual interest rate percentage.
* `tenureMonths` (Integer): Repayment tenure in months.
* `monthlyInstallment` (BigDecimal): Calculated reducing balance EMI.
* `outstandingPrincipal` (BigDecimal): Current unpaid principal balance.
* `outstandingInterest` (BigDecimal): Current accrued interest.
* `sureties` (Array of Embedded Documents): Linked guarantor member details.
* `status` (String): `APPLIED`, `APPROVED`, `DISBURSED`, `ACTIVE`, `CLOSED`, `SETTLED_WITH_MEMBER`.

### Collection: `deposit_accounts`
* `_id` (ObjectId): Primary key.
* `depositAccountNo` (String, Indexed, Unique): E.g. `FXD-10001`, `RCD-20001`.
* `memberCode` (String, Indexed): Depositor member code.
* `depositType` (String): `FXD`, `RCD`, `MIS`.
* `principalAmount` (BigDecimal): Contract principal amount.
* `interestRate` (Double): Annual interest rate percentage.
* `compoundingFrequency` (String): `QUARTERLY`, `HALF_YEARLY`, `YEARLY`, `SIMPLE`.
* `depositDate` (LocalDate): Date deposit was booked.
* `maturityDate` (LocalDate): Computed maturity date (adhering to 3rd-of-month cutoff rule).
* `maturityAmount` (BigDecimal): Computed maturity value.
* `status` (String): `ACTIVE`, `MATURED`, `PREMATURE_CLOSED`.

### Collection: `daily_accounts_processing`
* `_id` (ObjectId): Primary key.
* `processingDate` (LocalDate, Unique): Accounting date.
* `openingCashBalance` (BigDecimal): Vault cash at start of day.
* `totalCashReceipts` (BigDecimal): Cash inflows recorded today.
* `totalCashPayments` (BigDecimal): Cash outflows recorded today.
* `closingCashBalance` (BigDecimal): Reconciled ending cash vault balance.
* `dayStatus` (String): `OPEN`, `CLOSED`, `LOCKED`.

---

# 8. TESTING SUITE, EDGE CASES & VERIFICATION

Run the automated test suite anytime using:
```bash
./mvnw test
```

### Verified Test Cases
1. **Loan EMI Mathematical Accuracy (`FinancialMathTest`):**
   * Verifies reducing balance EMI calculation against standard banking benchmarks.
2. **Compound Interest Precision (`FinancialMathTest`):**
   * Confirms quarterly compounding formulas match legacy `SP_Calculations` results down to the exact paise.
3. **Legacy 3rd-of-the-Month Cutoff Date Validation (`FinancialMathTest`):**
   * Validates that deposits booked on the 2nd mature exactly on month $+ n$, while deposits booked on the 15th roll over to the 1st of month $+ n + 1$.
4. **Demand Schedule Gross Compilation (`MonthlyRecoveryEngineTest`):**
   * Validates arithmetic summation of Thrift + Principal + Interest + RCD.
5. **Shortage Variance Detection (`MonthlyRecoveryEngineTest`):**
   * Verifies that partial payroll deductions flag negative variances and log records to the `WrongPayment` discrepancy register.
6. **Cash Vault Mathematical Balancing (`DayBookBalancingTest`):**
   * Verifies that $\text{Opening Cash} + \text{Receipts} - \text{Payments} = \text{Closing Cash}$.

---

# 9. PRODUCTION CLOUD HOSTING & DEPLOYMENT MANUAL

### 9.1 Multi-Stage Dockerfile Architecture
The project includes an optimized multi-stage `Dockerfile`:
* **Stage 1 (Builder):** Uses `eclipse-temurin:21-jdk-alpine`, caches Maven dependencies, and compiles the application.
* **Stage 2 (Runtime):** Uses lightweight `eclipse-temurin:21-jre-alpine`, creates a non-root user (`amortix`), and runs `java -jar app.jar` with optimized JVM memory flags (`-XX:+UseG1GC`, `-XX:MaxRAMPercentage=75.0`).

### 9.2 Deploying to Render.com (1-Click Blueprint)
1. Push your repository to GitHub: `https://github.com/Sureekutty/Amortix-Prime`.
2. In the [Render Dashboard](https://dashboard.render.com), click **New +** $\rightarrow$ **Blueprint**.
3. Select the `Sureekutty/Amortix-Prime` repository.
4. Render automatically reads `render.yaml` and provisions the Docker service.
5. In the Render environment settings, configure your cloud database URI:
   * `MONGODB_URI`: `mongodb+srv://<user>:<password>@cluster.mongodb.net/amortix_prime_db?retryWrites=true&w=majority`
   * `PORT`: `8080`
   * `SPRING_PROFILES_ACTIVE`: `prod`
6. Click **Deploy**. Your service will be live online with automatic SSL.

---

# 10. SENIOR SOFTWARE ENGINEER INTERVIEW MASTERCLASS (Q&A)

#### Q1: "Why did you build AMORTIX | PRIME as a Monolith instead of Microservices?"
> *"For a cooperative banking core, transactions must be strictly consistent. Microservices introduce network latency, distributed transaction complexity (Sagas/Two-Phase Commit), and eventual consistency risks. By building a modular Monolith, we execute all financial ledger operations within atomic database transactions, achieving extreme performance, zero network hops, and operational simplicity."*

#### Q2: "Why did you choose MongoDB over SQL for a financial system?"
> *"In our legacy system, viewing a single member required joining 5 tables (`Members`, `MemAddress`, `Nominee`, `NomineeRef`, `MemberBank`), causing severe N+1 query bottlenecks. In MongoDB, these naturally map to an embedded document model, allowing complete 360° customer profiles to be retrieved in a single indexed read. Furthermore, for ledger operations, we leverage MongoDB's multi-document ACID transactions with Spring's `@Transactional` to guarantee full financial integrity."*

#### Q3: "Why is there zero JavaScript on the frontend?"
> *"In banking and enterprise fintech, security and reliability are paramount. Client-side JavaScript introduces XSS vulnerabilities, npm supply chain attack vectors, and fragile browser state management. By using pure server-side rendering with Thymeleaf and CSS3 tokens, every validation and financial check runs strictly on the Java server. Modals are powered by pure CSS `:target` pseudo-classes, resulting in a blisteringly fast, accessible, and tamper-proof interface."*

#### Q4: "How did you prevent rounding errors in loan interest calculations?"
> *"We strictly prohibited primitive `float` and `double` for currency. Floating-point numbers use binary approximations that cause representation drift (e.g. 0.1 + 0.2 = 0.30000000000000004). In banking, this breaks general ledger audits. We exclusively utilized `java.math.BigDecimal` configured with `RoundingMode.HALF_UP` to match statutory banking regulations."*

#### Q5: "How does the Monthly Recovery Reconciliation handle partial payroll deductions?"
> *"We implemented a statutory waterfall allocation priority: partial deductions are first allocated to the mandatory Compulsory Thrift Fund, then to outstanding Loan Interest, then to Loan Principal, and finally to Recurring Deposits. Any unrecovered shortage is automatically logged into the `WrongPayment` discrepancy register for arrears tracking."*

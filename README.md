# AMORTIX | PRIME
### Cooperative Credit & Core Ledger Architecture
[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL 8/9](https://img.shields.io/badge/MySQL-8.x%20%2F%209.x-blue.svg)](https://www.mysql.com/)
[![Spring Security 6](https://img.shields.io/badge/Security-Spring%20Security%206-blue.svg)](https://spring.io/projects/spring-security)
[![Zero-JS Frontend](https://img.shields.io/badge/Frontend-Pure%20HTML5%20%2B%20CSS3%20(Zero--JS)-informational.svg)]()
[![Production Ready](https://img.shields.io/badge/Deployment-Docker%20%2F%20Render%20%2F%20Railway-blueviolet.svg)]()

> **AMORTIX | PRIME** is an enterprise-grade Core Banking and Ledger Management Platform specifically architected for Cooperative Credit Societies, Employee Thrift Societies, and Mutual Benefit Lending Organizations. 

---

## ⚡ The Zero-JavaScript Monolithic Paradigm
Unlike typical modern apps that depend on fragile client-side JavaScript frameworks, AMORTIX | PRIME is engineered as a **100% Server-Side Rendered (SSR) Monolith**:
* **Backend:** Java 21 LTS, Spring Boot 3, Spring Data JPA / MySQL, Spring Security 6.
* **Frontend:** Semantic HTML5 templates rendered server-side via Thymeleaf, styled with custom CSS3 custom properties (variables), Flexbox, and CSS Grid.
* **Pure CSS Interactivity:** Modals via CSS `:target`, accordions via native HTML5 `<details>`, and pure server-side form validations.
* **Zero JavaScript:** Immune to client-side XSS vulnerabilities, zero npm dependency bloat, and unified server runtime.

---

## 🏛️ Comprehensive Feature Matrix (100% Legacy Parity)
AMORTIX | PRIME restores and enhances every module from the legacy Sybase/JSP system (`Societyold`):

1. **Member 360° Management:**
   * KYC, Employment, Basic Salary, Nominees, Addresses, and Bank Disbursement particulars.
   * Share Capital allotment and tracking.
   * Full & Final Retirement Settlement engine (`amortix.SP_MemberSettlementNew`).
2. **Credit & Lending Engine:**
   * Products: Long Term Loan (`LTL`), Executive / Express Loan (`EXL`), Emergency Loan (`ELG`), Fixed Deposit Loan (`FDL`).
   * Reducing Balance EMI computation.
   * Multi-Surety Guarantor management and replacement workflow (`suretyReplace.jsp`).
   * Automated PDF Sanction Order generation.
3. **Term & Recurring Deposits:**
   * Products: Fixed Deposit (`FXD`), Recurring Cumulative Deposit (`RCD`), Monthly Income Scheme (`MIS`).
   * Quarterly compounding adhering to legacy 3rd-of-the-month cutoff date rules (`amortix.SP_Calculations`).
   * Premature withdrawal with automated penalty rate deduction (`FXP`).
4. **Compulsory Monthly Thrift Fund:**
   * Monthly thrift savings deductions.
   * Annual compound thrift interest accrual and batch posting (`amortix.sp_ThriftInterestUpload`).
5. **Monthly Recovery & Reconciliation Engine:**
   * Automatic generation of monthly salary deduction Demand Schedules for employer payroll (`amortix.MonthlyProcess`).
   * Text/CSV export of billing demands.
   * Employer payroll response file upload and statutory waterfall reconciliation.
   * Under/over-deduction discrepancy logging (`amortix.WrongPayment`).
6. **Double-Entry Accounting & Treasury Day Book:**
   * Daily Accounts Processing, opening cash, inflows, outflows, closing balance, and day-end lock (`amortix.DailyAccountsProcessing`).
   * Receipts, Payments, and Journal Vouchers (`JV`).
   * Live Chart of Accounts & Trial Balance.
7. **Governance & Audit Trail:**
   * Dynamic Society Rules Engine (`amortix.Rules`) & Interest Rate Master (`amortix.Interest`).
   * Cooperative AGM Resolutions and Ballot Voting (`amortix.savePoll`).
   * Immutable Transaction Audit Trail (`amortix.TBSC_EVENTS`).

---

## 🚀 Quick Start Guide

### Prerequisites
* Java 21 LTS installed
* MySQL 8.x / 9.x running locally on port `3306` (or cloud MySQL connection URL)

### Local Launch
```bash
# Clone the repository
git clone https://github.com/Sureekutty/Amortix-Prime.git
cd Amortix-Prime

# Run with Maven Wrapper
./mvnw clean spring-boot:run
```

Access the application in your browser at: `http://localhost:8080`

### Demo Credentials
| Role | Username | Password |
|---|---|---|
| **System Administrator** | `admin` | `admin123` |
| **Loan Operations Officer** | `staff` | `staff123` |
| **Cooperative Member** | `member` | `member123` |

---

## 🧪 Automated Testing
Run the comprehensive test suite verifying financial math formulas, recovery reconciliations, and day book balancing:
```bash
./mvnw test
```

---

## ☁️ Cloud Deployment (Render / Railway / Docker)
* **Docker Compose:** `docker-compose up --build` launches both MySQL and the application container.
* **Render.com:** Uses `render.yaml` and multi-stage `Dockerfile`. Set `SPRING_DATASOURCE_URL` pointing to your cloud MySQL instance.

---



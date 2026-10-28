# Feature: Cooperative Governance, Rules Engine & Audit Trail

## Branch Overview
- **Branch Name:** `feature/admin-governance-audit`
- **Target Integration:** `develop`
- **Module:** Governance, Policy & Compliance

## Components & Architecture
1. **Society Business Rules Engine (`SocietyRule.java`):**
   - Configurable society-wide thresholds without recompilation:
     - Maximum loan sanction multipliers against basic pay.
     - Maximum simultaneous loan count per member.
     - Minimum required share capital percentage.
     - Mandatory thrift monthly subscription baselines.
2. **Master Interest Rate Tables (`InterestRateRule.java`):**
   - Product-specific effective rates with validity dates.
   - Fixed Deposit tier rates (by tenure range).
   - Loan interest rates by loan category (`LTL`, `EXL`, `FDL`).
3. **AGM Ballot Voting & Resolutions (`Poll.java`):**
   - Digital AGM resolution ballots for cooperative members.
   - Real-time tallying of votes with one-member-one-vote validation.
4. **Immutable Audit Event Logging (`AuditLog.java`):**
   - High-throughput audit logging for sensitive financial mutations (loan sanctions, cash payouts, rule edits).
   - Captures timestamp, acting user ID, IP address, action type, entity ID, and state diff.
5. **Web Controllers:**
   - `AdminController.java`: `/admin/rules`, `/admin/interest-rates`, `/admin/polls`, `/admin/audit-logs`.
   - `DashboardController.java`: `/dashboard` aggregate KPI metrics.
6. **UI Templates:**
   - `templates/admin/rules.html`: Dynamic rule modifier panel.
   - `templates/admin/interest_rates.html`: Live rate configuration table.
   - `templates/admin/polls.html`: Ballot creation and voting outcome analytics.
   - `templates/admin/audit_logs.html`: Filterable system event audit viewer.
   - `templates/dashboard.html`: Executive portfolio dashboard.

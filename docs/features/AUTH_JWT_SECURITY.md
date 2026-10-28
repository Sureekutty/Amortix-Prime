# Feature: Authentication, JWT Security & Role-Based Access Control (RBAC)

## Branch Overview
- **Branch Name:** `feature/auth-jwt-security`
- **Target Integration:** `develop`
- **Module:** Security & Identity Management

## Components & Architecture
1. **Security Configuration (`SecurityConfig.java`):**
   - Built on Spring Security 6.x filter chain.
   - Form-based login with BCrypt password hashing.
   - Role-based authorization rules:
     - `ROLE_ADMIN`: Full access to admin governance, rules, interest rates, audit logs, and accounting.
     - `ROLE_STAFF`: Access to member onboarding, loan origination, deposit servicing, and voucher entry.
     - `ROLE_MEMBER`: Self-service portal for member statement viewing and loan repayment status.
2. **User Details Service (`UserDetailsServiceImpl.java`):**
   - Integrates with database user accounts via `UserAccountRepository`.
   - Dynamic authority mapping for authenticated principals.
3. **Authentication Controller (`AuthController.java`):**
   - Login routing (`/login`), session creation, authentication error handling, and secure logout.
4. **Data Entities & Models:**
   - `UserAccount.java`: Username, BCrypt password hash, enabled flag, roles set, member linkage.
5. **UI Templates:**
   - `templates/login.html`: Responsive corporate login interface with enterprise theme and CSRF protection.

## Verification
- Unit & integration testing with Spring Security test harness.

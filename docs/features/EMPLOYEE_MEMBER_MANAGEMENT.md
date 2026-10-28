# Feature: Employee & Member Lifecycle Management

## Branch Overview
- **Branch Name:** `feature/employee-member-management`
- **Target Integration:** `develop`
- **Module:** Member & Employee Registry

## Components & Architecture
1. **Domain Model (`Member.java`):**
   - Member Identification: Member Account Number (`memAccNo`), Employee Code (`memEmpCode`).
   - Personal Demographics: Full Name, Father/Husband Name, Gender, Date of Birth.
   - Employment Profile: Department, Section, Designation, Date of Joining, Date of Superannuation/Retirement.
   - Financial Parameters: Basic Salary, Net Take-Home Pay, Share Capital Holdings, Monthly Thrift Deduction.
   - KYC & Nominee Linkages: Residential/Official Addresses, Primary & Secondary Nominees with guardian specifications.
2. **Member Service Layer (`MemberService.java` & `MemberServiceImpl.java`):**
   - `registerMember()`: Validates uniqueness of Employee Code and creates initial cooperative share capital.
   - `updateEmploymentDetails()`: Manages promotions, transfers, and basic pay increment modifications.
   - `calculateSettlement()`: Computes Full & Final retirement settlement balance:
     $$\text{Net Payout} = \text{Share Capital} + \text{Thrift Balance} + \text{Accrued Interest} - \text{Outstanding Loans}$$
3. **Web Controller (`MemberController.java`):**
   - Routing: `/members`, `/members/new`, `/members/{id}`, `/members/{id}/edit`, `/members/{id}/settlement`.
4. **UI Templates:**
   - `templates/members/list.html`: Paginated member directory with dynamic search and filtering.
   - `templates/members/form.html`: Form for onboarding new employee members.
   - `templates/members/view.html`: Detailed 360-degree member dossier.
   - `templates/members/settlement.html`: Full & final settlement statement generator with PDF export.

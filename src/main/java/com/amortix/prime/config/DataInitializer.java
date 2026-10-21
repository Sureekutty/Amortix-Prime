package com.amortix.prime.config;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepo;
    private final MemberRepository memberRepo;
    private final LoanRepository loanRepo;
    private final DepositAccountRepository depositRepo;
    private final SocietyRuleRepository ruleRepo;
    private final InterestRateRuleRepository interestRepo;
    private final GeneralLedgerAccountRepository ledgerRepo;
    private final DailyAccountProcessingRepository dailyRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserAccountRepository userRepo,
                           MemberRepository memberRepo,
                           LoanRepository loanRepo,
                           DepositAccountRepository depositRepo,
                           SocietyRuleRepository ruleRepo,
                           InterestRateRuleRepository interestRepo,
                           GeneralLedgerAccountRepository ledgerRepo,
                           DailyAccountProcessingRepository dailyRepo,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.loanRepo = loanRepo;
        this.depositRepo = depositRepo;
        this.ruleRepo = ruleRepo;
        this.interestRepo = interestRepo;
        this.ledgerRepo = ledgerRepo;
        this.dailyRepo = dailyRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedRules();
        seedInterestRates();
        seedChartOfAccounts();
        seedInitialDayBook();
        seedSampleMembersAndLoans();
    }

    private void seedUsers() {
        if (!userRepo.existsByUsername("admin")) {
            UserAccount admin = new UserAccount("admin", passwordEncoder.encode("admin123"), "System Administrator", "admin@amortix.prime", Set.of("ROLE_ADMIN", "ROLE_STAFF"));
            userRepo.save(admin);
        }
        if (!userRepo.existsByUsername("staff")) {
            UserAccount staff = new UserAccount("staff", passwordEncoder.encode("staff123"), "Loan Operations Officer", "staff@amortix.prime", Set.of("ROLE_STAFF"));
            userRepo.save(staff);
        }
        if (!userRepo.existsByUsername("member")) {
            UserAccount memberUser = new UserAccount("member", passwordEncoder.encode("member123"), "Sureendhar J", "sureendhar@society.coop", Set.of("ROLE_MEMBER"));
            memberUser.setMemberCode("MEM-1001");
            userRepo.save(memberUser);
        }
    }

    private void seedRules() {
        if (ruleRepo.count() == 0) {
            ruleRepo.save(new SocietyRule("MAX_LOAN_MULTIPLIER", "Maximum Loan Multiplier", "LOAN", "10", "Max loan sanction up to 10x of Basic Pay"));
            ruleRepo.save(new SocietyRule("MAX_SURETIES_PER_MEMBER", "Max Guarantor Sureties", "LOAN", "3", "Max active loan guarantees per member"));
            ruleRepo.save(new SocietyRule("MIN_SERVICE_MONTHS", "Min Service Required", "MEMBERSHIP", "6", "Minimum months of service for loan eligibility"));
            ruleRepo.save(new SocietyRule("SHARE_FACE_VALUE", "Share Face Value", "MEMBERSHIP", "100", "Face value per cooperative share in INR"));
            ruleRepo.save(new SocietyRule("MIN_SHARE_CAPITAL", "Minimum Share Capital", "MEMBERSHIP", "1000", "Minimum share capital needed for membership"));
        }
    }

    private void seedInterestRates() {
        if (interestRepo.count() == 0) {
            interestRepo.save(new InterestRateRule("LTL", "Long Term Loan", "LOAN", 12, 120, 9.0, "REDUCING_BALANCE"));
            interestRepo.save(new InterestRateRule("EXL", "Executive / Express Loan", "LOAN", 6, 36, 10.5, "REDUCING_BALANCE"));
            interestRepo.save(new InterestRateRule("ELG", "Emergency Loan", "LOAN", 3, 12, 8.5, "SIMPLE"));
            interestRepo.save(new InterestRateRule("FDL", "Fixed Deposit Loan", "LOAN", 6, 60, 8.0, "REDUCING_BALANCE"));
            interestRepo.save(new InterestRateRule("FXD", "Fixed Deposit", "DEPOSIT", 6, 120, 7.5, "COMPOUND_QUARTERLY"));
            interestRepo.save(new InterestRateRule("RCD", "Recurring Deposit", "DEPOSIT", 12, 60, 7.0, "COMPOUND_QUARTERLY"));
            interestRepo.save(new InterestRateRule("THRIFT", "Compulsory Thrift Fund", "SAVINGS", 12, 120, 6.0, "COMPOUND_YEARLY"));
            interestRepo.save(new InterestRateRule("FXP", "Premature Deposit Penalty", "DEPOSIT", 1, 120, 1.5, "PENALTY"));
        }
    }

    private void seedChartOfAccounts() {
        if (ledgerRepo.count() == 0) {
            ledgerRepo.save(new GeneralLedgerAccount("1001", "Cash in Hand", "ASSET", "DEBIT", new BigDecimal("50000.00")));
            ledgerRepo.save(new GeneralLedgerAccount("1002", "State Bank of India Current A/c", "ASSET", "DEBIT", new BigDecimal("2500000.00")));
            ledgerRepo.save(new GeneralLedgerAccount("2001", "Member Loans Outstanding (LTL/EXL)", "ASSET", "DEBIT", new BigDecimal("5000000.00")));
            ledgerRepo.save(new GeneralLedgerAccount("3001", "Member Thrift Deposit Liability", "LIABILITY", "CREDIT", new BigDecimal("4500000.00")));
            ledgerRepo.save(new GeneralLedgerAccount("3002", "Member Fixed Deposits Liability", "LIABILITY", "CREDIT", new BigDecimal("2000000.00")));
            ledgerRepo.save(new GeneralLedgerAccount("4001", "Paid-Up Share Capital", "EQUITY", "CREDIT", new BigDecimal("1050000.00")));
            ledgerRepo.save(new GeneralLedgerAccount("5001", "Loan Interest Income", "INCOME", "CREDIT", BigDecimal.ZERO));
            ledgerRepo.save(new GeneralLedgerAccount("6001", "Deposit & Thrift Interest Expense", "EXPENSE", "DEBIT", BigDecimal.ZERO));
            ledgerRepo.save(new GeneralLedgerAccount("6002", "Administrative & Audit Expense", "EXPENSE", "DEBIT", BigDecimal.ZERO));
        }
    }

    private void seedInitialDayBook() {
        if (dailyRepo.count() == 0) {
            DailyAccountProcessing today = new DailyAccountProcessing(LocalDate.now(), new BigDecimal("50000.00"), new BigDecimal("2500000.00"));
            dailyRepo.save(today);
        }
    }

    private void seedSampleMembersAndLoans() {
        if (memberRepo.count() == 0) {
            Member m1 = new Member();
            m1.setMemberCode("MEM-1001");
            m1.setEmployeeCode("EMP-9821");
            m1.setFullName("Sureendhar J");
            m1.setFatherOrSpouseName("Jayaraman");
            m1.setGender("MALE");
            m1.setDob(LocalDate.of(1995, 5, 15));
            m1.setJoiningDate(LocalDate.of(2020, 2, 10));
            m1.setDepartment("Information Technology");
            m1.setDesignation("Lead Systems Architect");
            m1.setBasicPay(new BigDecimal("65000.00"));
            m1.setGrossPay(new BigDecimal("95000.00"));
            m1.setNetPay(new BigDecimal("78000.00"));
            m1.setTotalShares(50);
            m1.setShareCapitalBalance(new BigDecimal("5000.00"));
            m1.setMonthlyThriftAmount(new BigDecimal("2500.00"));
            m1.setThriftBalance(new BigDecimal("85000.00"));
            m1.getAddresses().add(new Member.Address("PRESENT", "124 Anna Salai", "Chennai", "Tamil Nadu", "600002", "9876543210"));
            m1.getNominees().add(new Member.Nominee("Vasanthi J", "Mother", 55, new BigDecimal("100.00")));
            m1.setBankDetail(new Member.BankDetail("State Bank of India", "Anna Salai", "30987654321", "SBIN0001234"));
            memberRepo.save(m1);

            Member m2 = new Member();
            m2.setMemberCode("MEM-1002");
            m2.setEmployeeCode("EMP-9822");
            m2.setFullName("Kavitha Ramesh");
            m2.setFatherOrSpouseName("Ramesh Kumar");
            m2.setGender("FEMALE");
            m2.setDob(LocalDate.of(1992, 8, 20));
            m2.setJoiningDate(LocalDate.of(2019, 6, 1));
            m2.setDepartment("Accounts & Finance");
            m2.setDesignation("Senior Accountant");
            m2.setBasicPay(new BigDecimal("52000.00"));
            m2.setGrossPay(new BigDecimal("75000.00"));
            m2.setNetPay(new BigDecimal("61000.00"));
            m2.setTotalShares(30);
            m2.setShareCapitalBalance(new BigDecimal("3000.00"));
            m2.setMonthlyThriftAmount(new BigDecimal("2000.00"));
            m2.setThriftBalance(new BigDecimal("65000.00"));
            m2.getAddresses().add(new Member.Address("PRESENT", "45 Gandhi Road", "Coimbatore", "Tamil Nadu", "641001", "9845012345"));
            m2.getNominees().add(new Member.Nominee("Ramesh Kumar", "Husband", 35, new BigDecimal("100.00")));
            m2.setBankDetail(new Member.BankDetail("Canara Bank", "Gandhipuram", "10987654321", "CNRB0002345"));
            memberRepo.save(m2);

            Member m3 = new Member();
            m3.setMemberCode("MEM-1003");
            m3.setEmployeeCode("EMP-9823");
            m3.setFullName("Anand Raj");
            m3.setFatherOrSpouseName("Sundaram");
            m3.setGender("MALE");
            m3.setDob(LocalDate.of(1988, 11, 5));
            m3.setJoiningDate(LocalDate.of(2015, 3, 15));
            m3.setDepartment("Operations");
            m3.setDesignation("Operations Manager");
            m3.setBasicPay(new BigDecimal("78000.00"));
            m3.setGrossPay(new BigDecimal("110000.00"));
            m3.setNetPay(new BigDecimal("89000.00"));
            m3.setTotalShares(100);
            m3.setShareCapitalBalance(new BigDecimal("10000.00"));
            m3.setMonthlyThriftAmount(new BigDecimal("3000.00"));
            m3.setThriftBalance(new BigDecimal("145000.00"));
            memberRepo.save(m3);

            // Sample Active Loan for MEM-1001
            Loan loan1 = new Loan();
            loan1.setLoanAccountNo("LTL-10001");
            loan1.setMemberCode("MEM-1001");
            loan1.setMemberName("Sureendhar J");
            loan1.setEmployeeCode("EMP-9821");
            loan1.setDepartment("Information Technology");
            loan1.setLoanType("LTL");
            loan1.setLoanPurpose("House Renovation");
            loan1.setSanctionAmount(new BigDecimal("400000.00"));
            loan1.setInterestRate(9.0);
            loan1.setTenureMonths(48);
            loan1.setMonthlyInstallment(new BigDecimal("9956.00"));
            loan1.setOutstandingPrincipal(new BigDecimal("350000.00"));
            loan1.setOutstandingInterest(BigDecimal.ZERO);
            loan1.setStatus("ACTIVE");
            loan1.setSanctionDate(LocalDate.now().minusMonths(6));
            loan1.setDisbursementDate(LocalDate.now().minusMonths(6));
            loan1.setRecoveryStartDate(LocalDate.now().minusMonths(5).withDayOfMonth(1));
            loan1.getSureties().add(new Loan.LoanSurety("MEM-1002", "Kavitha Ramesh", "EMP-9822", new BigDecimal("52000.00")));
            loan1.getSureties().add(new Loan.LoanSurety("MEM-1003", "Anand Raj", "EMP-9823", new BigDecimal("78000.00")));
            loanRepo.save(loan1);

            m2.setActiveSuretyCount(1);
            memberRepo.save(m2);
            m3.setActiveSuretyCount(1);
            memberRepo.save(m3);

            // Sample Fixed Deposit for MEM-1002
            DepositAccount dep1 = new DepositAccount();
            dep1.setDepositAccountNo("FXD-10001");
            dep1.setMemberCode("MEM-1002");
            dep1.setMemberName("Kavitha Ramesh");
            dep1.setDepositType("FXD");
            dep1.setPrincipalAmount(new BigDecimal("100000.00"));
            dep1.setTenureMonths(24);
            dep1.setInterestRate(7.5);
            dep1.setCompoundingFrequency("QUARTERLY");
            dep1.setDepositDate(LocalDate.now().minusMonths(4));
            dep1.setMaturityDate(LocalDate.now().plusMonths(20));
            dep1.setMaturityAmount(new BigDecimal("116029.00"));
            dep1.setAccumulatedInterest(new BigDecimal("16029.00"));
            dep1.setTotalPaidIn(new BigDecimal("100000.00"));
            dep1.setStatus("ACTIVE");
            depositRepo.save(dep1);
        }
    }
}

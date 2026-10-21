package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Cooperative Member Master Document.
 * Corresponds to legacy speccs.Members, speccs.MembersBasic, speccs.MemAddress, speccs.Nominee
 */
@Document(collection = "members")
public class Member {

    @Id
    private String id;

    @Indexed(unique = true)
    private String memberCode; // e.g. MEM-1001

    @Indexed
    private String employeeCode; // e.g. EMP-9821
    private String fullName;
    private String fatherOrSpouseName;
    private String gender; // MALE, FEMALE, OTHER
    private LocalDate dob;
    private LocalDate joiningDate;
    private LocalDate retirementDate;
    private String department;
    private String designation;

    // Financial salary details
    private BigDecimal basicPay = BigDecimal.ZERO;
    private BigDecimal grossPay = BigDecimal.ZERO;
    private BigDecimal netPay = BigDecimal.ZERO;

    // Cooperative balances
    private BigDecimal shareCapitalBalance = BigDecimal.ZERO;
    private Integer totalShares = 0;
    private BigDecimal monthlyThriftAmount = new BigDecimal("1000.00");
    private BigDecimal thriftBalance = BigDecimal.ZERO;

    // Status: ACTIVE, INACTIVE, RETIRED, SETTLED, SUSPENDED
    private String status = "ACTIVE";

    // Surety stats
    private int activeSuretyCount = 0; // Number of active loans guaranteed by this member

    // Addresses (Present, Permanent, Office)
    private List<Address> addresses = new ArrayList<>();

    // Nominees
    private List<Nominee> nominees = new ArrayList<>();

    // Bank Account details for loan disbursement / dividend / settlements
    private BankDetail bankDetail = new BankDetail();

    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime updatedDate = LocalDateTime.now();

    public Member() {}

    // Embedded Domain Value Objects
    public static class Address {
        private String addressType; // PRESENT, PERMANENT, OFFICE
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String mobileNumber;
        private String email;

        public Address() {}

        public Address(String addressType, String addressLine1, String city, String state, String pincode, String mobileNumber) {
            this.addressType = addressType;
            this.addressLine1 = addressLine1;
            this.city = city;
            this.state = state;
            this.pincode = pincode;
            this.mobileNumber = mobileNumber;
        }

        public String getAddressType() { return addressType; }
        public void setAddressType(String addressType) { this.addressType = addressType; }
        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }
        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class Nominee {
        private String nomineeName;
        private String relationship;
        private int age;
        private BigDecimal sharePercentage = new BigDecimal("100.00");
        private String guardianName;
        private String guardianRelation;

        public Nominee() {}

        public Nominee(String nomineeName, String relationship, int age, BigDecimal sharePercentage) {
            this.nomineeName = nomineeName;
            this.relationship = relationship;
            this.age = age;
            this.sharePercentage = sharePercentage;
        }

        public String getNomineeName() { return nomineeName; }
        public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public BigDecimal getSharePercentage() { return sharePercentage; }
        public void setSharePercentage(BigDecimal sharePercentage) { this.sharePercentage = sharePercentage; }
        public String getGuardianName() { return guardianName; }
        public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
        public String getGuardianRelation() { return guardianRelation; }
        public void setGuardianRelation(String guardianRelation) { this.guardianRelation = guardianRelation; }
    }

    public static class BankDetail {
        private String bankName;
        private String branch;
        private String accountNumber;
        private String ifscCode;

        public BankDetail() {}

        public BankDetail(String bankName, String branch, String accountNumber, String ifscCode) {
            this.bankName = bankName;
            this.branch = branch;
            this.accountNumber = accountNumber;
            this.ifscCode = ifscCode;
        }

        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getIfscCode() { return ifscCode; }
        public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getFatherOrSpouseName() { return fatherOrSpouseName; }
    public void setFatherOrSpouseName(String fatherOrSpouseName) { this.fatherOrSpouseName = fatherOrSpouseName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public LocalDate getRetirementDate() { return retirementDate; }
    public void setRetirementDate(LocalDate retirementDate) { this.retirementDate = retirementDate; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public BigDecimal getBasicPay() { return basicPay; }
    public void setBasicPay(BigDecimal basicPay) { this.basicPay = basicPay; }
    public BigDecimal getGrossPay() { return grossPay; }
    public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public BigDecimal getShareCapitalBalance() { return shareCapitalBalance; }
    public void setShareCapitalBalance(BigDecimal shareCapitalBalance) { this.shareCapitalBalance = shareCapitalBalance; }
    public Integer getTotalShares() { return totalShares; }
    public void setTotalShares(Integer totalShares) { this.totalShares = totalShares; }
    public BigDecimal getMonthlyThriftAmount() { return monthlyThriftAmount; }
    public void setMonthlyThriftAmount(BigDecimal monthlyThriftAmount) { this.monthlyThriftAmount = monthlyThriftAmount; }
    public BigDecimal getThriftBalance() { return thriftBalance; }
    public void setThriftBalance(BigDecimal thriftBalance) { this.thriftBalance = thriftBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getActiveSuretyCount() { return activeSuretyCount; }
    public void setActiveSuretyCount(int activeSuretyCount) { this.activeSuretyCount = activeSuretyCount; }
    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }
    public List<Nominee> getNominees() { return nominees; }
    public void setNominees(List<Nominee> nominees) { this.nominees = nominees; }
    public BankDetail getBankDetail() { return bankDetail; }
    public void setBankDetail(BankDetail bankDetail) { this.bankDetail = bankDetail; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}

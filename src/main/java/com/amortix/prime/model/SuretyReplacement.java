package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Surety Replacement Workflow.
 * Replaces a guarantor on an active loan with another eligible member.
 * Corresponds to legacy suretyReplace.jsp and amortix.Surety updates.
 */
@Document(collection = "surety_replacements")
public class SuretyReplacement {

    @Id
    private String id;

    @Indexed
    private String loanAccountNo;

    @Indexed
    private String borrowerMemberCode;
    private String borrowerName;

    private String oldSuretyMemberCode;
    private String oldSuretyName;

    private String newSuretyMemberCode;
    private String newSuretyName;

    private String reasonForReplacement; // Resignation, Retirement, Mutual agreement, Incapacity
    private LocalDate requestDate = LocalDate.now();

    // Status: PENDING, APPROVED, REJECTED
    private String status = "PENDING";
    private String approvedBy;
    private LocalDate approvalDate;
    private String remarks;

    private LocalDateTime createdDate = LocalDateTime.now();

    public SuretyReplacement() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLoanAccountNo() { return loanAccountNo; }
    public void setLoanAccountNo(String loanAccountNo) { this.loanAccountNo = loanAccountNo; }
    public String getBorrowerMemberCode() { return borrowerMemberCode; }
    public void setBorrowerMemberCode(String borrowerMemberCode) { this.borrowerMemberCode = borrowerMemberCode; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public String getOldSuretyMemberCode() { return oldSuretyMemberCode; }
    public void setOldSuretyMemberCode(String oldSuretyMemberCode) { this.oldSuretyMemberCode = oldSuretyMemberCode; }
    public String getOldSuretyName() { return oldSuretyName; }
    public void setOldSuretyName(String oldSuretyName) { this.oldSuretyName = oldSuretyName; }
    public String getNewSuretyMemberCode() { return newSuretyMemberCode; }
    public void setNewSuretyMemberCode(String newSuretyMemberCode) { this.newSuretyMemberCode = newSuretyMemberCode; }
    public String getNewSuretyName() { return newSuretyName; }
    public void setNewSuretyName(String newSuretyName) { this.newSuretyName = newSuretyName; }
    public String getReasonForReplacement() { return reasonForReplacement; }
    public void setReasonForReplacement(String reasonForReplacement) { this.reasonForReplacement = reasonForReplacement; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}

package com.amortix.prime.service;

import com.amortix.prime.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface MemberService {
    Member registerMember(Member member);
    Member updateMember(Member member);
    Member findByMemberCode(String memberCode);
    Member findById(String id);
    Page<Member> listMembers(String search, Pageable pageable);
    List<Member> listActiveMembers();
    boolean existsByMemberCode(String memberCode);

    void addShareCapital(String memberCode, int shares, BigDecimal shareAmount);
    void updateThriftSubscription(String memberCode, BigDecimal newMonthlyAmount);

    // Legacy SP_MemberSettlementNew: final account settlement upon retirement/resignation
    MemberSettlementResult calculateAndExecuteSettlement(String memberCode, String remarks, String operatorUser);

    record MemberSettlementResult(
            String memberCode,
            String memberName,
            BigDecimal shareCapitalRefund,
            BigDecimal thriftRefund,
            BigDecimal depositRefund,
            BigDecimal loanPrincipalDeduction,
            BigDecimal loanInterestDeduction,
            BigDecimal netPayableAmount,
            String status
    ) {}
}

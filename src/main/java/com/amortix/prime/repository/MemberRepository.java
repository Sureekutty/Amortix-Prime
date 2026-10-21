package com.amortix.prime.repository;

import com.amortix.prime.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByMemberCode(String memberCode);
    Optional<Member> findByEmployeeCode(String employeeCode);
    boolean existsByMemberCode(String memberCode);
    boolean existsByEmployeeCode(String employeeCode);

    List<Member> findByStatus(String status);
    Page<Member> findByStatus(String status, Pageable pageable);

    Page<Member> findByFullNameContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrEmployeeCodeContainingIgnoreCase(
            String name, String memberCode, String empCode, Pageable pageable);
}

package com.amortix.prime.repository;

import com.amortix.prime.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByVoucherNo(String voucherNo);
    List<Voucher> findByVoucherDate(LocalDate date);
    List<Voucher> findByVoucherDateBetween(LocalDate from, LocalDate to);
    List<Voucher> findByMemberCode(String memberCode);
    Page<Voucher> findByVoucherType(String voucherType, Pageable pageable);
    Page<Voucher> findAllByOrderByVoucherDateDesc(Pageable pageable);
    long countByVoucherType(String voucherType);
}

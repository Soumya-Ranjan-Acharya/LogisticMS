package com.truckmate.repository;

import com.truckmate.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByEmailAndUsedFalseOrderByExpiryDesc(String email);
    void deleteByEmail(String email);
}

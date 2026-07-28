package com.backend.Skytouch.payment.repository;

import com.backend.Skytouch.payment.entity.Payment;
import com.backend.Skytouch.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByReference(String reference);
    boolean existsByReference(String reference);
}

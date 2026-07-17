package com.truckmate.repository;

import com.truckmate.entity.SkippedOrder;
import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkippedOrderRepository extends JpaRepository<SkippedOrder, Long> {
    boolean existsByDriverAndOrder(User driver, Order order);
    Optional<SkippedOrder> findByDriverAndOrder(User driver, Order order);
}

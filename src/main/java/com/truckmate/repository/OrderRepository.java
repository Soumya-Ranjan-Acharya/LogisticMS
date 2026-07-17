package com.truckmate.repository;

import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByTransporterOrderByCreatedAtDesc(User transporter);
    List<Order> findByDriverAndStatusOrderByCompletedAtDesc(User driver, Order.OrderStatus status);
    Optional<Order> findByDriverAndStatusIn(User driver, List<Order.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.id NOT IN " +
           "(SELECT s.order.id FROM SkippedOrder s WHERE s.driver = :driver) " +
           "ORDER BY o.createdAt ASC")
    List<Order> findPendingOrdersNotSkippedByDriver(@Param("driver") User driver);

    long countByStatus(Order.OrderStatus status);
    long countByDriver(User driver);
}

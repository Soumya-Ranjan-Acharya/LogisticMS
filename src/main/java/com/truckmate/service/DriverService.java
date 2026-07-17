package com.truckmate.service;

import com.truckmate.entity.Notification;
import com.truckmate.entity.Order;
import com.truckmate.entity.SkippedOrder;
import com.truckmate.entity.User;
import com.truckmate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    @Autowired private UserRepository userRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private SkippedOrderRepository skippedRepo;
    @Autowired private NotificationRepository notifRepo;

    /** Returns the driver's current active/in-transit job, if any. */
    public Optional<Order> getCurrentJob(User driver) {
        return orderRepo.findByDriverAndStatusIn(driver,
                List.of(Order.OrderStatus.ACCEPTED, Order.OrderStatus.IN_TRANSIT));
    }

    /** Returns pending orders that this driver has NOT skipped. */
    public List<Order> getAvailableOrders(User driver) {
        return orderRepo.findPendingOrdersNotSkippedByDriver(driver);
    }

    /** Returns all completed orders for this driver. */
    public List<Order> getCompletedOrders(User driver) {
        return orderRepo.findByDriverAndStatusOrderByCompletedAtDesc(driver, Order.OrderStatus.COMPLETED);
    }

    /**
     * Accept an order: only allowed if this driver has turnActive=true,
     * order is still PENDING, and driver has no current job.
     */
    @Transactional
    public String acceptOrder(Long orderId, User driver) {
        if (!Boolean.TRUE.equals(driver.getTurnActive())) {
            return "error:It is not your turn to accept orders.";
        }
        Optional<Order> jobOpt = getCurrentJob(driver);
        if (jobOpt.isPresent()) {
            return "error:You already have an active job. Complete it first.";
        }
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "error:Order not found.";
        }
        Order order = orderOpt.get();
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return "error:This order is no longer available.";
        }

        order.setDriver(driver);
        order.setStatus(Order.OrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        orderRepo.save(order);

        // Notify transporter
        Notification tn = new Notification(order.getTransporter(),
                "Order #" + orderId + " has been accepted by driver " + driver.getName() + ".");
        notifRepo.save(tn);

        // Rotate turn to next driver
        rotateTurn(driver);

        return "success";
    }

    /**
     * Reject (skip) an order: marks it as skipped for this driver.
     * If this driver has skipped ALL available orders, rotate turn.
     */
    @Transactional
    public String rejectOrder(Long orderId, User driver) {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) return "error:Order not found.";

        Order order = orderOpt.get();
        if (!skippedRepo.existsByDriverAndOrder(driver, order)) {
            skippedRepo.save(new SkippedOrder(driver, order));
        }

        // Check if all pending orders are now skipped by this driver → rotate
        List<Order> remaining = orderRepo.findPendingOrdersNotSkippedByDriver(driver);
        if (remaining.isEmpty() && Boolean.TRUE.equals(driver.getTurnActive())) {
            rotateTurn(driver);
            // Notify driver that turn was passed
            Notification n = new Notification(driver, "Your turn has passed. Waiting for next cycle.");
            notifRepo.save(n);
        }

        return "success";
    }

    /** Start delivery: ACCEPTED → IN_TRANSIT */
    @Transactional
    public String startDelivery(Long orderId, User driver) {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) return "error:Order not found.";
        Order order = orderOpt.get();
        if (!driver.getId().equals(order.getDriver().getId())) return "error:Access denied.";
        if (order.getStatus() != Order.OrderStatus.ACCEPTED) return "error:Order is not in ACCEPTED state.";

        order.setStatus(Order.OrderStatus.IN_TRANSIT);
        order.setStartedAt(LocalDateTime.now());
        orderRepo.save(order);

        // Notify transporter
        notifRepo.save(new Notification(order.getTransporter(),
                "Order #" + orderId + " is now IN TRANSIT by " + driver.getName() + "."));
        return "success";
    }

    /** Complete delivery: IN_TRANSIT → COMPLETED */
    @Transactional
    public String completeDelivery(Long orderId, User driver) {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) return "error:Order not found.";
        Order order = orderOpt.get();
        if (!driver.getId().equals(order.getDriver().getId())) return "error:Access denied.";
        if (order.getStatus() != Order.OrderStatus.IN_TRANSIT) return "error:Order is not IN_TRANSIT.";

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepo.save(order);

        // Notify transporter
        notifRepo.save(new Notification(order.getTransporter(),
                "Order #" + orderId + " has been COMPLETED by " + driver.getName() + "."));
        return "success";
    }

    /** Return all drivers ordered by priority. */
    public List<User> getAllDrivers() {
        return userRepo.findByRoleOrderByPriorityAsc(User.Role.DRIVER);
    }

    /** Get notifications for a user. */
    public List<Notification> getNotifications(User user) {
        List<Notification> notifs = notifRepo.findByUserOrderByCreatedAtDesc(user);
        // Mark all as read
        notifs.forEach(n -> n.setRead(true));
        notifRepo.saveAll(notifs);
        return notifs;
    }

    public long countUnread(User user) {
        return notifRepo.countByUserAndIsReadFalse(user);
    }

    /**
     * Rotate active turn to next driver in priority order (wraps around).
     */
    private void rotateTurn(User currentDriver) {
        currentDriver.setTurnActive(false);
        userRepo.save(currentDriver);

        List<User> drivers = userRepo.findByRoleOrderByPriorityAsc(User.Role.DRIVER);
        if (drivers.isEmpty()) return;

        int currentIdx = -1;
        for (int i = 0; i < drivers.size(); i++) {
            if (drivers.get(i).getId().equals(currentDriver.getId())) {
                currentIdx = i;
                break;
            }
        }

        int nextIdx = (currentIdx + 1) % drivers.size();
        User nextDriver = drivers.get(nextIdx);
        nextDriver.setTurnActive(true);
        userRepo.save(nextDriver);

        // Notify next driver
        Notification n = new Notification(nextDriver,
                "🚛 It's your turn! You can now accept orders.");
        notifRepo.save(n);
    }
}

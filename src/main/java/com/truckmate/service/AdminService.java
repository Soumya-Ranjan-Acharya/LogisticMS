package com.truckmate.service;

import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import com.truckmate.repository.OrderRepository;
import com.truckmate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private UserRepository userRepo;

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepo.findByStatus(status);
    }

    public List<User> getAllDrivers() {
        return userRepo.findByRoleOrderByPriorityAsc(User.Role.DRIVER);
    }

    public List<User> getAllTransporters() {
        return userRepo.findByRoleOrderByPriorityAsc(User.Role.TRANSPORTER);
    }

    public long totalOrders()     { return orderRepo.count(); }
    public long pendingOrders()   { return orderRepo.countByStatus(Order.OrderStatus.PENDING); }
    public long activeOrders()    { return orderRepo.countByStatus(Order.OrderStatus.ACCEPTED)
                                         + orderRepo.countByStatus(Order.OrderStatus.IN_TRANSIT); }
    public long completedOrders() { return orderRepo.countByStatus(Order.OrderStatus.COMPLETED); }
    public long totalDrivers()    { return userRepo.countByRole(User.Role.DRIVER); }
    public long totalTransporters() { return userRepo.countByRole(User.Role.TRANSPORTER); }
}

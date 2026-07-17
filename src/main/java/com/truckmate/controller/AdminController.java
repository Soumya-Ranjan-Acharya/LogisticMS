package com.truckmate.controller;

import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import com.truckmate.repository.UserRepository;
import com.truckmate.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private UserRepository userRepo;

    private User getAdmin(HttpSession session) {
        Long id = (Long) session.getAttribute("userId");
        return userRepo.findById(id).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("admin", admin);
        model.addAttribute("totalOrders",      adminService.totalOrders());
        model.addAttribute("pendingOrders",    adminService.pendingOrders());
        model.addAttribute("activeOrders",     adminService.activeOrders());
        model.addAttribute("completedOrders",  adminService.completedOrders());
        model.addAttribute("totalDrivers",     adminService.totalDrivers());
        model.addAttribute("totalTransporters",adminService.totalTransporters());
        model.addAttribute("activeTab", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(value = "status", required = false) String status,
                         HttpSession session, Model model) {
        User admin = getAdmin(session);
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = adminService.getOrdersByStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } else {
            orders = adminService.getAllOrders();
        }
        model.addAttribute("admin", admin);
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("activeTab", "orders");
        return "admin/orders";
    }

    @GetMapping("/drivers")
    public String drivers(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("admin", admin);
        model.addAttribute("drivers", adminService.getAllDrivers());
        model.addAttribute("activeTab", "drivers");
        return "admin/drivers";
    }

    @GetMapping("/transporters")
    public String transporters(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("admin", admin);
        model.addAttribute("transporters", adminService.getAllTransporters());
        model.addAttribute("activeTab", "transporters");
        return "admin/transporters";
    }
}

package com.truckmate.controller;

import com.truckmate.entity.Notification;
import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import com.truckmate.repository.OrderRepository;
import com.truckmate.repository.UserRepository;
import com.truckmate.service.DriverService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/driver")
public class DriverController {

    @Autowired private DriverService driverService;
    @Autowired private UserRepository userRepo;
    @Autowired private OrderRepository orderRepo;

    // Helper: get current logged-in driver from session
    private User getDriver(HttpSession session) {
        Long id = (Long) session.getAttribute("userId");
        return userRepo.findById(id).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User driver = getDriver(session);
        model.addAttribute("driver", driver);
        model.addAttribute("activeTab", "dashboard");
        model.addAttribute("unreadCount", driverService.countUnread(driver));
        model.addAttribute("currentJob", driverService.getCurrentJob(driver).orElse(null));
        model.addAttribute("availableCount", driverService.getAvailableOrders(driver).size());
        return "driver/dashboard";
    }

    @GetMapping("/orders")
    public String availableOrders(HttpSession session, Model model) {
        User driver = getDriver(session);
        List<Order> orders = driverService.getAvailableOrders(driver);
        model.addAttribute("driver", driver);
        model.addAttribute("orders", orders);
        model.addAttribute("activeTab", "orders");
        model.addAttribute("unreadCount", driverService.countUnread(driver));
        return "driver/orders";
    }

    @GetMapping("/order-detail/{id}")
    public String orderDetail(@PathVariable("id") Long id, HttpSession session, Model model) {
        User driver = getDriver(session);
        Optional<Order> orderOpt = orderRepo.findById(id);
        if (orderOpt.isEmpty()) return "redirect:/driver/orders";
        model.addAttribute("driver", driver);
        model.addAttribute("order", orderOpt.get());
        model.addAttribute("activeTab", "orders");
        return "driver/order-detail";
    }

    @PostMapping("/accept/{id}")
    public String acceptOrder(@PathVariable("id") Long id, HttpSession session, RedirectAttributes ra) {
        User driver = getDriver(session);
        String result = driverService.acceptOrder(id, driver);
        if (result.startsWith("error:")) {
            ra.addFlashAttribute("error", result.substring(6));
            return "redirect:/driver/orders";
        }
        ra.addFlashAttribute("success", "Order accepted! Start your delivery when ready.");
        return "redirect:/driver/current-job";
    }

    @PostMapping("/reject/{id}")
    public String rejectOrder(@PathVariable("id") Long id, HttpSession session, RedirectAttributes ra) {
        User driver = getDriver(session);
        String result = driverService.rejectOrder(id, driver);
        if (result.startsWith("error:")) {
            ra.addFlashAttribute("error", result.substring(6));
        } else {
            ra.addFlashAttribute("info", "Order skipped.");
        }
        return "redirect:/driver/orders";
    }

    @GetMapping("/current-job")
    public String currentJob(HttpSession session, Model model) {
        User driver = getDriver(session);
        Optional<Order> jobOpt = driverService.getCurrentJob(driver);
        model.addAttribute("driver", driver);
        model.addAttribute("currentJob", jobOpt.orElse(null));
        model.addAttribute("activeTab", "current-job");
        model.addAttribute("unreadCount", driverService.countUnread(driver));
        return "driver/current-job";
    }

    @PostMapping("/start/{id}")
    public String startDelivery(@PathVariable("id") Long id, HttpSession session, RedirectAttributes ra) {
        User driver = getDriver(session);
        String result = driverService.startDelivery(id, driver);
        if (result.startsWith("error:")) {
            ra.addFlashAttribute("error", result.substring(6));
        } else {
            ra.addFlashAttribute("success", "Delivery started! You are IN TRANSIT.");
        }
        return "redirect:/driver/current-job";
    }

    @PostMapping("/complete/{id}")
    public String completeDelivery(@PathVariable("id") Long id, HttpSession session, RedirectAttributes ra) {
        User driver = getDriver(session);
        String result = driverService.completeDelivery(id, driver);
        if (result.startsWith("error:")) {
            ra.addFlashAttribute("error", result.substring(6));
        } else {
            ra.addFlashAttribute("success", "Delivery completed! Great job.");
        }
        return "redirect:/driver/completed";
    }

    @GetMapping("/completed")
    public String completedJobs(HttpSession session, Model model) {
        User driver = getDriver(session);
        model.addAttribute("driver", driver);
        model.addAttribute("completedOrders", driverService.getCompletedOrders(driver));
        model.addAttribute("activeTab", "completed");
        model.addAttribute("unreadCount", driverService.countUnread(driver));
        return "driver/completed";
    }

    @GetMapping("/priority")
    public String priorityStatus(HttpSession session, Model model) {
        User driver = getDriver(session);
        model.addAttribute("driver", driver);
        model.addAttribute("allDrivers", driverService.getAllDrivers());
        model.addAttribute("activeTab", "priority");
        model.addAttribute("unreadCount", driverService.countUnread(driver));
        return "driver/priority";
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        User driver = getDriver(session);
        List<Notification> notifs = driverService.getNotifications(driver);
        model.addAttribute("driver", driver);
        model.addAttribute("notifications", notifs);
        model.addAttribute("activeTab", "notifications");
        model.addAttribute("unreadCount", 0);
        return "driver/notifications";
    }
}

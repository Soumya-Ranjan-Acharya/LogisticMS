package com.truckmate.controller;

import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import com.truckmate.repository.UserRepository;
import com.truckmate.service.TransporterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/transporter")
public class TransporterController {

    @Autowired private TransporterService transporterService;
    @Autowired private UserRepository userRepo;

    private User getTransporter(HttpSession session) {
        Long id = (Long) session.getAttribute("userId");
        return userRepo.findById(id).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User t = getTransporter(session);
        List<Order> orders = transporterService.getMyOrders(t);
        long pending   = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count();
        long active    = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.ACCEPTED ||
                                                     o.getStatus() == Order.OrderStatus.IN_TRANSIT).count();
        long completed = orders.stream().filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED).count();

        model.addAttribute("transporter", t);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("pendingOrders", pending);
        model.addAttribute("activeOrders", active);
        model.addAttribute("completedOrders", completed);
        model.addAttribute("activeTab", "dashboard");
        return "transporter/dashboard";
    }

    @GetMapping("/create-order")
    public String createOrderPage(HttpSession session, Model model) {
        model.addAttribute("transporter", getTransporter(session));
        model.addAttribute("activeTab", "create-order");
        return "transporter/create-order";
    }

    @PostMapping("/create-order")
    public String submitOrder(HttpSession session,
                              @RequestParam("goodsType") String goodsType,
                              @RequestParam(value = "weight", required = false) Double weight,
                              @RequestParam("vehicleType") String vehicleType,
                              @RequestParam("pickupLocation") String pickupLocation,
                              @RequestParam("dropLocation") String dropLocation,
                              @RequestParam(value = "preferredTime", required = false) String preferredTime,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              RedirectAttributes ra) {
        User t = getTransporter(session);
        try {
            Order order = transporterService.createOrder(t, goodsType, weight, vehicleType,
                    pickupLocation, dropLocation, preferredTime, image);
            ra.addFlashAttribute("success", "Order #" + order.getId() + " created successfully! Drivers will be notified.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }
        return "redirect:/transporter/my-orders";
    }

    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        User t = getTransporter(session);
        model.addAttribute("transporter", t);
        model.addAttribute("orders", transporterService.getMyOrders(t));
        model.addAttribute("activeTab", "my-orders");
        return "transporter/my-orders";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User t = getTransporter(session);
        model.addAttribute("transporter", t);
        model.addAttribute("activeTab", "profile");
        return "transporter/profile";
    }
}

package com.truckmate.controller;

import com.truckmate.entity.User;
import com.truckmate.service.AuthService;
import com.truckmate.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"/", "/login"})
    public String loginPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return redirectByRole((String) session.getAttribute("role"));
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("email") String email,
                          @RequestParam("password") String password,
                          HttpSession session,
                          Model model) {
        Optional<User> userOpt = authService.validateLogin(email, password);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("email", email);
            return "login";
        }
        User user = userOpt.get();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("role", user.getRole().name());
        session.setMaxInactiveInterval(3600); // 1 hour
        return redirectByRole(user.getRole().name());
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email,
                          RedirectAttributes ra) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("otpError", "No account found with this email.");
        } else {
            String code = authService.generateOtp(email);
            // In production: send email. For demo, show the code on screen.
            ra.addFlashAttribute("otpSent", true);
            ra.addFlashAttribute("otpCode", code);  // Demo only
            ra.addFlashAttribute("otpEmail", email);
        }
        return "redirect:/login";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("email") String email,
                            @RequestParam("otp") String otp,
                            HttpSession session,
                            Model model,
                            RedirectAttributes ra) {
        if (!authService.validateOtp(email, otp)) {
            ra.addFlashAttribute("otpError", "Invalid or expired OTP.");
            ra.addFlashAttribute("otpEmail", email);
            return "redirect:/login";
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Account not found.");
            return "redirect:/login";
        }
        User user = userOpt.get();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("role", user.getRole().name());
        return redirectByRole(user.getRole().name());
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }

    private String redirectByRole(String role) {
        return switch (role) {
            case "DRIVER"      -> "redirect:/driver/dashboard";
            case "TRANSPORTER" -> "redirect:/transporter/dashboard";
            case "ADMIN"       -> "redirect:/admin/dashboard";
            default            -> "redirect:/login";
        };
    }
}

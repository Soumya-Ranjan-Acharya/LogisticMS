package com.truckmate.service;

import com.truckmate.entity.Otp;
import com.truckmate.entity.User;
import com.truckmate.repository.OtpRepository;
import com.truckmate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    /**
     * Validates email + password. Returns User if valid, empty otherwise.
     */
    public Optional<User> validateLogin(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPasswordHash().equals(password));
    }

    /**
     * Generates a 6-digit OTP, stores it with 10-min expiry, returns the code.
     * In a real system this would be emailed; here it's returned to display on screen.
     */
    @Transactional
    public String generateOtp(String email) {
        // Expire any existing OTPs for this email
        otpRepository.deleteByEmail(email);

        String code = String.format("%06d", new Random().nextInt(999999));
        Otp otp = new Otp(email, code, LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);
        return code;
    }

    /**
     * Returns true if OTP is valid and not expired.
     */
    @Transactional
    public boolean validateOtp(String email, String code) {
        Optional<Otp> otpOpt = otpRepository.findTopByEmailAndUsedFalseOrderByExpiryDesc(email);
        if (otpOpt.isEmpty()) return false;

        Otp otp = otpOpt.get();
        if (!otp.getCode().equals(code)) return false;
        if (otp.getExpiry().isBefore(LocalDateTime.now())) return false;

        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }
}

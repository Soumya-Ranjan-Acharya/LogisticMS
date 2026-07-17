package com.truckmate.config;

import com.truckmate.entity.User;
import com.truckmate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public ApplicationRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) {
                return; // already seeded
            }

            // Admin
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@truckmate.com");
            admin.setPasswordHash("admin123");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);

            // Drivers
            String[] driverNames = {"Ravi Kumar", "Suresh Mehta", "Arjun Singh"};
            String[] vehicles    = {"TN 01 AB 1234", "MH 02 CD 5678", "DL 03 EF 9012"};
            for (int i = 0; i < 3; i++) {
                User d = new User();
                d.setName(driverNames[i]);
                d.setEmail("driver" + (i + 1) + "@truckmate.com");
                d.setPasswordHash("driver123");
                d.setRole(User.Role.DRIVER);
                d.setVehicleNumber(vehicles[i]);
                d.setPriority(i + 1);
                d.setTurnActive(i == 0); // Driver 1 starts with active turn
                userRepository.save(d);
            }

            // Transporters
            String[] transNames    = {"Priya Logistics", "Sharma Transports"};
            String[] companyNames  = {"Priya Logistics Pvt Ltd", "Sharma Transports Co."};
            for (int i = 0; i < 2; i++) {
                User t = new User();
                t.setName(transNames[i]);
                t.setEmail("transporter" + (i + 1) + "@truckmate.com");
                t.setPasswordHash("trans123");
                t.setRole(User.Role.TRANSPORTER);
                t.setCompanyName(companyNames[i]);
                userRepository.save(t);
            }

            System.out.println("✅ TruckMate: Demo data seeded successfully.");
            System.out.println("   Admin:        admin@truckmate.com / admin123");
            System.out.println("   Drivers:      driver1..3@truckmate.com / driver123");
            System.out.println("   Transporters: transporter1..2@truckmate.com / trans123");
        };
    }
}

package com.truckmate.service;

import com.truckmate.entity.Order;
import com.truckmate.entity.User;
import com.truckmate.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransporterService {

    @Autowired private OrderRepository orderRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional
    public Order createOrder(User transporter, String goodsType, Double weight,
                             String vehicleType, String pickupLocation, String dropLocation,
                             String preferredTime, MultipartFile image) throws IOException {
        Order order = new Order();
        order.setTransporter(transporter);
        order.setGoodsType(goodsType);
        order.setWeight(weight);
        order.setVehicleType(vehicleType);
        order.setPickupLocation(pickupLocation);
        order.setDropLocation(dropLocation);
        order.setPreferredTime(preferredTime);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), uploadPath.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            order.setImagePath("/uploads/" + filename);
        }

        return orderRepo.save(order);
    }

    public List<Order> getMyOrders(User transporter) {
        return orderRepo.findByTransporterOrderByCreatedAtDesc(transporter);
    }
}

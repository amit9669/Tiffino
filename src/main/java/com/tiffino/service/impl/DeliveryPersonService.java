package com.tiffino.service.impl;

import com.tiffino.entity.Delivery;
import com.tiffino.entity.DeliveryPerson;
import com.tiffino.entity.DeliveryStatus;
import com.tiffino.entity.Manager;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.DeliveryPersonRepository;
import com.tiffino.repository.DeliveryRepository;
import com.tiffino.service.IDeliveryPersonService;
import com.tiffino.service.OtpService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class DeliveryPersonService implements IDeliveryPersonService {
    
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public Object updatePassword(String email, int otp, String newPassword) {
        if (deliveryPersonRepository.existsByEmail(email)) {
            DeliveryPerson deliveryPerson = deliveryPersonRepository.findByEmail(email).get();
            if (otpService.getOtp(deliveryPerson.getEmail()) == otp) {
                otpService.clearOTP(deliveryPerson.getEmail());
                System.out.println("New Password : " + newPassword);
                deliveryPerson.setPassword(passwordEncoder.encode(newPassword));
                deliveryPersonRepository.save(deliveryPerson);
                return "Password Updated Successfully!!";
            } else {
                return "OTP NOT MATCHED!!";
            }
        } else {
            return "Incorrect Id!!";
        }
    }

    public void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(message);
            javaMailSender.send(email);
        } catch (CustomException e) {
            log.error("Exception while send Email ", e);
        }
    }

    @Override
    public Object pickupOrder(Long deliveryId) {
        Delivery delivery = deliveryRepository.findByOrder_OrderId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            return "Order cannot be picked up, current status: " + delivery.getStatus();
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpAt(LocalDateTime.now());

        delivery.getOrder().setOrderStatus("OUT_FOR_DELIVERY"); // update order status

        deliveryRepository.save(delivery);
        return "OUT_FOR_DELIVERY";
    }

    @Override
    public Object deliverOrder(Long deliveryId) {
        Delivery delivery = deliveryRepository.findByOrder_OrderId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            return "Order cannot be delivered, current status: " + delivery.getStatus();
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        delivery.getOrder().setOrderStatus("DELIVERED");

        DeliveryPerson dp = delivery.getDeliveryPerson();
        dp.setIsAvailable(true);
        deliveryPersonRepository.save(dp);

         deliveryRepository.save(delivery);
        return "DELIVERED";
    }
}

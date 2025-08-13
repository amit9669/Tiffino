package com.tiffino.service.impl;

import com.tiffino.config.AuthenticationService;
import com.tiffino.config.JwtService;
import com.tiffino.entity.Manager;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.CloudKitchenRepository;
import com.tiffino.repository.ManagerRepository;
import com.tiffino.service.DataToken;
import com.tiffino.service.IManagerService;
import com.tiffino.service.OtpService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ManagerService implements IManagerService {

    @Autowired
    private OtpService otpService;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DataToken dataToken;

    @Autowired
    private CloudKitchenRepository kitchenRepository;

    @Autowired
    private JavaMailSender javaMailSender;


    @Override
    public Object updatePassword(String managerId, int otp, String newPassword) {
        if (managerRepository.existsById(managerId)) {
            Manager manager = managerRepository.findById(managerId).get();
            if (otpService.getOtp(manager.getManagerEmail()) == otp) {
                otpService.clearOTP(manager.getManagerEmail());
                System.out.println("New Password : " + newPassword);
                manager.setPassword(passwordEncoder.encode(newPassword));
                managerRepository.save(manager);
                return "Password Updated Successfully!!";
            } else {
                return "OTP NOT MATCHED!!";
            }
        } else {
            return "Incorrect Id!!";
        }
    }

    @Override
    public Object forgotPasswordOfManager(String email, HttpSession session) {
        System.out.println(email);
        if(managerRepository.existsByManagerEmail(email)){
            this.sendEmail(email, "For Update Password", "This is your OTP :- " + otpService.generateOTP(email));
            session.setAttribute("email", email);
            return "Check email for OTP verification!";
        }else{
            return "This Email not exists!! First Create account!!";
        }
    }

    @Override
    public Object changePassword(int otp, String newPassword, String confirmNewPassword, HttpSession session) {
        if (otpService.getOtp((String) session.getAttribute("email"))==otp){
            Manager manager = managerRepository.findByManagerEmail((String) session.getAttribute("email")).get();
            if(newPassword.equals(confirmNewPassword)){
                manager.setPassword(passwordEncoder.encode(newPassword));
                return "Password has changed!!";
            }else{
                return "password doesn't match!! Please Try Again!!";
            }
        }else{
            return "OTP not Matched!!!";
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
    public Object getDataOfCloudKitchen() {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();
        return kitchenRepository.findById(manager.getCloudKitchen().getCloudKitchenId()).get();
    }
}

package com.tiffino.service;

import jakarta.servlet.http.HttpSession;

public interface IManagerService {

    Object updatePassword(String managerId, int otp, String newPassword);

    Object forgotPasswordOfManager(String email, HttpSession session);

    Object changePassword(int otp, String newPassword, String confirmNewPassword, HttpSession session);

    Object getDataOfCloudKitchen();
}

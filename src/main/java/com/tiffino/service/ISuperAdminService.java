package com.tiffino.service;

import com.tiffino.entity.request.CloudKitchenRequest;
import com.tiffino.entity.request.ManagerRequest;
import com.tiffino.entity.request.SubscriptionRequest;
import com.tiffino.entity.request.SuperAdminRequest;

import java.util.List;

public interface ISuperAdminService {

    Object saveOrUpdateAdmin(SuperAdminRequest superAdminRequest);

    Object saveOrUpdateManager(ManagerRequest managerRequest);

    Object saveOrUpdateCloudKitchen(CloudKitchenRequest kitchenRequest);

    Object deleteCloudKitchen(String kitchenId);

    Object deleteManager(String managerId);

    Object searchFilterForAdmin(List<String> state, List<String> city, List<String> division);

    Object saveOrUpdateSubscriptionPlan(SubscriptionRequest subscriptionRequest);
}

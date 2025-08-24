package com.tiffino.service;

import com.tiffino.entity.Offer;
import com.tiffino.entity.request.*;

import java.util.List;

public interface ISuperAdminService {

    Object saveOrUpdateAdmin(SuperAdminRequest superAdminRequest);

    Object saveOrUpdateManager(ManagerRequest managerRequest);

    Object saveOrUpdateCloudKitchen(CloudKitchenRequest kitchenRequest);

    Object deleteCloudKitchen(String kitchenId);

    Object deleteManager(String managerId);

    Object searchFilterForAdmin(List<String> state, List<String> city, List<String> division);

    Object saveOrUpdateSubscriptionPlan(SubscriptionRequest subscriptionRequest);

    Object saveOrUpdateDeliveryPerson(DeliveryPersonRequest personRequest);

    Object listOfIsAvailable();
}

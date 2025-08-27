package com.tiffino.service;

import com.tiffino.entity.Offer;
import com.tiffino.entity.request.*;

import java.io.IOException;
import java.util.List;

public interface ISuperAdminService {

    Object updateAdmin(SuperAdminRequest superAdminRequest);

    Object saveManager(ManagerRequest managerRequest);

    Object getAllManagersWithCloudKitchen();

    Object saveCloudKitchen(CloudKitchenRequest kitchenRequest);

    Object deleteCloudKitchen(String kitchenId);

    Object deleteManager(String managerId);

    Object searchFilterForAdmin(List<String> state, List<String> city, List<String> division);

    Object saveOrUpdateSubscriptionPlan(SubscriptionRequest subscriptionRequest);

    Object getAllSubscription();

    Object saveOrUpdateDeliveryPerson(DeliveryPersonRequest personRequest);

    Object saveOrUpdateCuisine(CuisineRequest cuisineRequest) throws IOException;

    Object saveOrUpdateMeal(MealRequest mealRequest);

    Object createOffer(OfferRequest offerRequest);

    Object getAllOffers();

    Object assignOffersToEligibleUsers(Long offerId);

    Object getAllOffersWithRedeemUsers();

    Object getAllSubscribedUser();

    Object getAllCloudKItchenAndReviews();

    Object getAllCuisines();
}

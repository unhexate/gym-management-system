package com.gym.service;

/**
 * Strategy Pattern (Behavioral) – Pricing interface.
 *
 * Each concrete implementation encapsulates a pricing algorithm for a
 * specific membership plan type. MembershipService selects the appropriate
 * strategy at runtime based on the plan name.
 */
public interface PricingStrategy {
    /**
     * Calculate the final price from the plan's base price.
     *
     * @param basePrice the original price defined on the MembershipPlan
     * @return the price to charge the member after applying any discount
     */
    double calculatePrice(double basePrice);
}

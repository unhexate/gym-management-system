package com.gym.service;

/**
 * Concrete Strategy – Basic plan pricing.
 * No discount applied; the base price is returned as-is.
 */
public class BasicPricing implements PricingStrategy {

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice; // no discount for basic plan
    }
}

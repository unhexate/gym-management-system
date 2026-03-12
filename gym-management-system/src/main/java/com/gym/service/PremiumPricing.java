package com.gym.service;

/**
 * Concrete Strategy – Premium plan pricing.
 * Applies a 20 % discount to the base price.
 */
public class PremiumPricing implements PricingStrategy {

    private static final double DISCOUNT_RATE = 0.20;

    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * (1 - DISCOUNT_RATE); // 20 % off for premium plan
    }
}

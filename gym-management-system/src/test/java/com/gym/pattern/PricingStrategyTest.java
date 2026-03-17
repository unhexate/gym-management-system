package com.gym.pattern;

import com.gym.service.BasicPricing;
import com.gym.service.PremiumPricing;
import com.gym.service.PricingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Strategy Pattern (PricingStrategy implementations).
 * No Spring context needed – pure arithmetic.
 */
@DisplayName("PricingStrategy – Strategy Pattern")
class PricingStrategyTest {

    @Test
    @DisplayName("BasicPricing returns base price unchanged")
    void basicPricingNoDiscount() {
        PricingStrategy strategy = new BasicPricing();
        assertEquals(100.0, strategy.calculatePrice(100.0), 0.001);
        assertEquals(250.0, strategy.calculatePrice(250.0), 0.001);
    }

    @Test
    @DisplayName("PremiumPricing applies 20% discount")
    void premiumPricingAppliesDiscount() {
        PricingStrategy strategy = new PremiumPricing();
        assertEquals(80.0,  strategy.calculatePrice(100.0), 0.001);
        assertEquals(200.0, strategy.calculatePrice(250.0), 0.001);
    }

    @Test
    @DisplayName("PremiumPricing discount is always 20% regardless of amount")
    void premiumDiscountRate() {
        PricingStrategy strategy = new PremiumPricing();
        double base = 500.0;
        double result = strategy.calculatePrice(base);
        assertEquals(base * 0.80, result, 0.001);
    }
}

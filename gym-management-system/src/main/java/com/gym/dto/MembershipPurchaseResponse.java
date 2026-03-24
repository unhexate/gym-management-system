package com.gym.dto;

import com.gym.model.Membership;
import com.gym.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MembershipPurchaseResponse {
    private Membership membership;
    private Payment payment;
}

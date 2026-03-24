package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseMembershipRequest {

    @NotNull(message = "planId is required")
    private Long planId;

    @NotBlank(message = "paymentMode is required")
    private String paymentMode;
}

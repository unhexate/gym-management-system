package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotBlank(message = "paymentStatus is required")
    private String paymentStatus;
}

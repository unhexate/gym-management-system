package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarkCheckoutRequest {

    @NotNull(message = "memberId is required")
    private Long memberId;

    @NotBlank(message = "checkoutTime is required (HH:mm format)")
    private String checkoutTime;
}

package com.gym.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SelfEnrollMembershipRequest {

    @NotNull(message = "planId is required")
    private Long planId;
}

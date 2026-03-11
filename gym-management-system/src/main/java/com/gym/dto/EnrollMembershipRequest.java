package com.gym.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EnrollMembershipRequest {

    @NotNull(message = "memberId is required")
    private Long memberId;

    @NotNull(message = "planId is required")
    private Long planId;
}

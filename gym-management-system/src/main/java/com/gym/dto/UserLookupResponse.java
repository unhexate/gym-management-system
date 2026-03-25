package com.gym.dto;

import com.gym.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLookupResponse {
    private Long id;
    private String name;
    private String email;
    private String role;

    public static UserLookupResponse from(User user) {
        return new UserLookupResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

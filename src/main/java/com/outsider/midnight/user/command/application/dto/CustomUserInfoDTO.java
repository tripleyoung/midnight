package com.outsider.midnight.user.command.application.dto;


import com.outsider.midnight.user.command.domain.aggregate.User;
import com.outsider.midnight.user.command.domain.aggregate.embeded.Authority;
import lombok.Data;

@Data
public class CustomUserInfoDTO {
    private Long userId;
    private String email;
    private Authority role;

    public CustomUserInfoDTO(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.role = user.getAuthority();
    }
}

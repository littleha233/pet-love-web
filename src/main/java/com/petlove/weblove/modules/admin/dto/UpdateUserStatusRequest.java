package com.petlove.weblove.modules.admin.dto;

import com.petlove.weblove.modules.user.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull
    private UserStatus status;

    @NotBlank
    private String reason;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

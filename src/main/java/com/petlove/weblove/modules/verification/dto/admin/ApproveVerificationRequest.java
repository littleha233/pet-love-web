package com.petlove.weblove.modules.verification.dto.admin;

import jakarta.validation.constraints.Size;

public class ApproveVerificationRequest {

    @Size(max = 200)
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

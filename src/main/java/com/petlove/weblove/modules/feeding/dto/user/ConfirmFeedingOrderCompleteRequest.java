package com.petlove.weblove.modules.feeding.dto.user;

import jakarta.validation.constraints.Size;

public class ConfirmFeedingOrderCompleteRequest {

    @Size(max = 255)
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

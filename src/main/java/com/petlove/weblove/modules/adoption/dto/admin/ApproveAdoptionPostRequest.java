package com.petlove.weblove.modules.adoption.dto.admin;

import jakarta.validation.constraints.Size;

public class ApproveAdoptionPostRequest {

    @Size(max = 200)
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

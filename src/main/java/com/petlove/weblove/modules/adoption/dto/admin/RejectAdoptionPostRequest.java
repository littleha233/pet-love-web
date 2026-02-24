package com.petlove.weblove.modules.adoption.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectAdoptionPostRequest {

    @NotBlank
    @Size(max = 64)
    private String rejectReasonCode;

    @NotBlank
    @Size(max = 255)
    private String rejectReasonText;

    @Size(max = 200)
    private String remark;

    public String getRejectReasonCode() {
        return rejectReasonCode;
    }

    public void setRejectReasonCode(String rejectReasonCode) {
        this.rejectReasonCode = rejectReasonCode;
    }

    public String getRejectReasonText() {
        return rejectReasonText;
    }

    public void setRejectReasonText(String rejectReasonText) {
        this.rejectReasonText = rejectReasonText;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

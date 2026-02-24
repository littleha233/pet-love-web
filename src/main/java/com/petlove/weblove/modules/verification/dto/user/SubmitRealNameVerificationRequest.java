package com.petlove.weblove.modules.verification.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubmitRealNameVerificationRequest {

    @NotBlank
    @Size(max = 32)
    private String realName;

    @NotBlank
    @Size(max = 64)
    private String idNo;

    @NotNull
    private Long idFrontFileId;

    @NotNull
    private Long idBackFileId;

    private Long holdingIdFileId;

    @AssertTrue(message = "must be true")
    private boolean agreeDeclaration;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public Long getIdFrontFileId() {
        return idFrontFileId;
    }

    public void setIdFrontFileId(Long idFrontFileId) {
        this.idFrontFileId = idFrontFileId;
    }

    public Long getIdBackFileId() {
        return idBackFileId;
    }

    public void setIdBackFileId(Long idBackFileId) {
        this.idBackFileId = idBackFileId;
    }

    public Long getHoldingIdFileId() {
        return holdingIdFileId;
    }

    public void setHoldingIdFileId(Long holdingIdFileId) {
        this.holdingIdFileId = holdingIdFileId;
    }

    public boolean isAgreeDeclaration() {
        return agreeDeclaration;
    }

    public void setAgreeDeclaration(boolean agreeDeclaration) {
        this.agreeDeclaration = agreeDeclaration;
    }
}

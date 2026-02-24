package com.petlove.weblove.modules.verification.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SubmitProviderVerificationRequest {

    @NotNull
    @Min(0)
    @Max(50)
    private Integer providerExperienceYears;

    @NotBlank
    @Size(max = 1000)
    private String providerIntro;

    @NotEmpty
    private List<@NotBlank String> providerServicePetTypes;

    @NotBlank
    private String providerServiceCityCode;

    private List<@NotBlank String> providerCapabilityTags;

    @Size(max = 6)
    private List<Long> supportingFileIds;

    @AssertTrue(message = "must be true")
    private boolean agreeServiceCode;

    public Integer getProviderExperienceYears() {
        return providerExperienceYears;
    }

    public void setProviderExperienceYears(Integer providerExperienceYears) {
        this.providerExperienceYears = providerExperienceYears;
    }

    public String getProviderIntro() {
        return providerIntro;
    }

    public void setProviderIntro(String providerIntro) {
        this.providerIntro = providerIntro;
    }

    public List<String> getProviderServicePetTypes() {
        return providerServicePetTypes;
    }

    public void setProviderServicePetTypes(List<String> providerServicePetTypes) {
        this.providerServicePetTypes = providerServicePetTypes;
    }

    public String getProviderServiceCityCode() {
        return providerServiceCityCode;
    }

    public void setProviderServiceCityCode(String providerServiceCityCode) {
        this.providerServiceCityCode = providerServiceCityCode;
    }

    public List<String> getProviderCapabilityTags() {
        return providerCapabilityTags;
    }

    public void setProviderCapabilityTags(List<String> providerCapabilityTags) {
        this.providerCapabilityTags = providerCapabilityTags;
    }

    public List<Long> getSupportingFileIds() {
        return supportingFileIds;
    }

    public void setSupportingFileIds(List<Long> supportingFileIds) {
        this.supportingFileIds = supportingFileIds;
    }

    public boolean isAgreeServiceCode() {
        return agreeServiceCode;
    }

    public void setAgreeServiceCode(boolean agreeServiceCode) {
        this.agreeServiceCode = agreeServiceCode;
    }
}

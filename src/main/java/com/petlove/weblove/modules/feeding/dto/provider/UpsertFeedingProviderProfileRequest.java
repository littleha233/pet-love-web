package com.petlove.weblove.modules.feeding.dto.provider;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class UpsertFeedingProviderProfileRequest {

    @Size(max = 32)
    private String status;

    @Size(max = 64)
    private String displayName;

    @Size(max = 128)
    private String headline;

    @Size(max = 2000)
    private String intro;

    @NotBlank
    @Size(max = 32)
    private String serviceCityCode;

    @NotBlank
    @Size(max = 64)
    private String serviceCityName;

    @Size(max = 30)
    private List<@NotBlank @Size(max = 64) String> serviceDistricts;

    @NotEmpty
    @Size(min = 1, max = 5)
    private List<@NotBlank @Size(max = 16) String> servicePetTypes;

    @NotEmpty
    @Size(min = 1, max = 10)
    private List<@NotBlank @Size(max = 32) String> serviceItemTags;

    @DecimalMin("0.00")
    @DecimalMax("99999999.99")
    private BigDecimal basePricePerVisit;

    @Min(0)
    @Max(50)
    private Integer experienceYears;

    @Min(1)
    @Max(50)
    private Integer maxOrdersPerDay;

    @Size(max = 255)
    private String acceptNotes;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getServiceCityCode() {
        return serviceCityCode;
    }

    public void setServiceCityCode(String serviceCityCode) {
        this.serviceCityCode = serviceCityCode;
    }

    public String getServiceCityName() {
        return serviceCityName;
    }

    public void setServiceCityName(String serviceCityName) {
        this.serviceCityName = serviceCityName;
    }

    public List<String> getServiceDistricts() {
        return serviceDistricts;
    }

    public void setServiceDistricts(List<String> serviceDistricts) {
        this.serviceDistricts = serviceDistricts;
    }

    public List<String> getServicePetTypes() {
        return servicePetTypes;
    }

    public void setServicePetTypes(List<String> servicePetTypes) {
        this.servicePetTypes = servicePetTypes;
    }

    public List<String> getServiceItemTags() {
        return serviceItemTags;
    }

    public void setServiceItemTags(List<String> serviceItemTags) {
        this.serviceItemTags = serviceItemTags;
    }

    public BigDecimal getBasePricePerVisit() {
        return basePricePerVisit;
    }

    public void setBasePricePerVisit(BigDecimal basePricePerVisit) {
        this.basePricePerVisit = basePricePerVisit;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Integer getMaxOrdersPerDay() {
        return maxOrdersPerDay;
    }

    public void setMaxOrdersPerDay(Integer maxOrdersPerDay) {
        this.maxOrdersPerDay = maxOrdersPerDay;
    }

    public String getAcceptNotes() {
        return acceptNotes;
    }

    public void setAcceptNotes(String acceptNotes) {
        this.acceptNotes = acceptNotes;
    }
}

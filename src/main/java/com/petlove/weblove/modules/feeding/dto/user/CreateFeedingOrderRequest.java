package com.petlove.weblove.modules.feeding.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateFeedingOrderRequest {

    @NotNull
    private Long providerUserId;

    @NotBlank
    @Size(max = 32)
    private String serviceCityCode;

    @NotBlank
    @Size(max = 64)
    private String serviceCityName;

    @Size(max = 64)
    private String serviceDistrictName;

    @NotBlank
    @Size(max = 255)
    private String serviceAddressDetail;

    @Size(max = 255)
    private String serviceAddressNote;

    @NotBlank
    @Size(max = 64)
    private String contactName;

    @NotBlank
    @Size(max = 32)
    private String contactMobile;

    @NotEmpty
    @Size(min = 1, max = 5)
    private List<@NotNull Long> petIds;

    @NotEmpty
    @Size(min = 1, max = 10)
    private List<@NotBlank @Size(max = 32) String> serviceItemTags;

    @Size(max = 2000)
    private String ownerNote;

    @DecimalMin("0.00")
    @DecimalMax("99999999.99")
    private BigDecimal requestedTotalAmount;

    @NotEmpty
    @Size(min = 1, max = 20)
    private List<@Valid CreateFeedingOrderVisitDTO> visits;

    public Long getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(Long providerUserId) {
        this.providerUserId = providerUserId;
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

    public String getServiceDistrictName() {
        return serviceDistrictName;
    }

    public void setServiceDistrictName(String serviceDistrictName) {
        this.serviceDistrictName = serviceDistrictName;
    }

    public String getServiceAddressDetail() {
        return serviceAddressDetail;
    }

    public void setServiceAddressDetail(String serviceAddressDetail) {
        this.serviceAddressDetail = serviceAddressDetail;
    }

    public String getServiceAddressNote() {
        return serviceAddressNote;
    }

    public void setServiceAddressNote(String serviceAddressNote) {
        this.serviceAddressNote = serviceAddressNote;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public List<Long> getPetIds() {
        return petIds;
    }

    public void setPetIds(List<Long> petIds) {
        this.petIds = petIds;
    }

    public List<String> getServiceItemTags() {
        return serviceItemTags;
    }

    public void setServiceItemTags(List<String> serviceItemTags) {
        this.serviceItemTags = serviceItemTags;
    }

    public String getOwnerNote() {
        return ownerNote;
    }

    public void setOwnerNote(String ownerNote) {
        this.ownerNote = ownerNote;
    }

    public BigDecimal getRequestedTotalAmount() {
        return requestedTotalAmount;
    }

    public void setRequestedTotalAmount(BigDecimal requestedTotalAmount) {
        this.requestedTotalAmount = requestedTotalAmount;
    }

    public List<CreateFeedingOrderVisitDTO> getVisits() {
        return visits;
    }

    public void setVisits(List<CreateFeedingOrderVisitDTO> visits) {
        this.visits = visits;
    }
}

package com.petlove.weblove.modules.feeding.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.feeding.enums.FeedingOrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "feeding_orders")
public class FeedingOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "provider_user_id", nullable = false)
    private Long providerUserId;

    @Column(name = "provider_profile_id", nullable = false)
    private Long providerProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedingOrderStatus status;

    @Column(name = "service_city_code", nullable = false)
    private String serviceCityCode;

    @Column(name = "service_city_name", nullable = false)
    private String serviceCityName;

    @Column(name = "service_district_name")
    private String serviceDistrictName;

    @Column(name = "service_address_detail", nullable = false)
    private String serviceAddressDetail;

    @Column(name = "service_address_note")
    private String serviceAddressNote;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_mobile", nullable = false)
    private String contactMobile;

    @Column(name = "contact_mobile_masked", nullable = false)
    private String contactMobileMasked;

    @Column(name = "service_item_tags", nullable = false, columnDefinition = "JSON")
    private String serviceItemTags;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount;

    @Column(name = "owner_note", columnDefinition = "TEXT")
    private String ownerNote;

    @Column(name = "requested_total_amount")
    private BigDecimal requestedTotalAmount;

    @Column(name = "quoted_total_amount")
    private BigDecimal quotedTotalAmount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "provider_response_note")
    private String providerResponseNote;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "owner_confirmed_at")
    private LocalDateTime ownerConfirmedAt;

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Long getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(Long providerUserId) {
        this.providerUserId = providerUserId;
    }

    public Long getProviderProfileId() {
        return providerProfileId;
    }

    public void setProviderProfileId(Long providerProfileId) {
        this.providerProfileId = providerProfileId;
    }

    public FeedingOrderStatus getStatus() {
        return status;
    }

    public void setStatus(FeedingOrderStatus status) {
        this.status = status;
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

    public String getContactMobileMasked() {
        return contactMobileMasked;
    }

    public void setContactMobileMasked(String contactMobileMasked) {
        this.contactMobileMasked = contactMobileMasked;
    }

    public String getServiceItemTags() {
        return serviceItemTags;
    }

    public void setServiceItemTags(String serviceItemTags) {
        this.serviceItemTags = serviceItemTags;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
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

    public BigDecimal getQuotedTotalAmount() {
        return quotedTotalAmount;
    }

    public void setQuotedTotalAmount(BigDecimal quotedTotalAmount) {
        this.quotedTotalAmount = quotedTotalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProviderResponseNote() {
        return providerResponseNote;
    }

    public void setProviderResponseNote(String providerResponseNote) {
        this.providerResponseNote = providerResponseNote;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getOwnerConfirmedAt() {
        return ownerConfirmedAt;
    }

    public void setOwnerConfirmedAt(LocalDateTime ownerConfirmedAt) {
        this.ownerConfirmedAt = ownerConfirmedAt;
    }
}

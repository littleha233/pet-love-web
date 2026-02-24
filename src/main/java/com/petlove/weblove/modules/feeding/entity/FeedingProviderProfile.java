package com.petlove.weblove.modules.feeding.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.feeding.enums.FeedingProviderProfileStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "feeding_provider_profiles")
public class FeedingProviderProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_user_id", nullable = false, unique = true)
    private Long providerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedingProviderProfileStatus status;

    @Column(name = "display_name")
    private String displayName;

    private String headline;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(name = "service_city_code", nullable = false)
    private String serviceCityCode;

    @Column(name = "service_city_name", nullable = false)
    private String serviceCityName;

    @Column(name = "service_districts", columnDefinition = "JSON")
    private String serviceDistricts;

    @Column(name = "service_pet_types", nullable = false, columnDefinition = "JSON")
    private String servicePetTypes;

    @Column(name = "service_item_tags", nullable = false, columnDefinition = "JSON")
    private String serviceItemTags;

    @Column(name = "base_price_per_visit")
    private BigDecimal basePricePerVisit;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "max_orders_per_day")
    private Integer maxOrdersPerDay;

    @Column(name = "accept_notes")
    private String acceptNotes;

    @Column(name = "rating_avg", nullable = false)
    private BigDecimal ratingAvg;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @Column(name = "completed_order_count", nullable = false)
    private Integer completedOrderCount;

    public Long getId() {
        return id;
    }

    public Long getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(Long providerUserId) {
        this.providerUserId = providerUserId;
    }

    public FeedingProviderProfileStatus getStatus() {
        return status;
    }

    public void setStatus(FeedingProviderProfileStatus status) {
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

    public String getServiceDistricts() {
        return serviceDistricts;
    }

    public void setServiceDistricts(String serviceDistricts) {
        this.serviceDistricts = serviceDistricts;
    }

    public String getServicePetTypes() {
        return servicePetTypes;
    }

    public void setServicePetTypes(String servicePetTypes) {
        this.servicePetTypes = servicePetTypes;
    }

    public String getServiceItemTags() {
        return serviceItemTags;
    }

    public void setServiceItemTags(String serviceItemTags) {
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

    public BigDecimal getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Integer getCompletedOrderCount() {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Integer completedOrderCount) {
        this.completedOrderCount = completedOrderCount;
    }
}

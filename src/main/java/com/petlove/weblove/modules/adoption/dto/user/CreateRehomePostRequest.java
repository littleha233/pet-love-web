package com.petlove.weblove.modules.adoption.dto.user;

import com.petlove.weblove.modules.adoption.enums.NeuteredStatus;
import com.petlove.weblove.modules.adoption.enums.PetGender;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.enums.VaccinatedStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateRehomePostRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String content;

    @NotBlank
    private String cityCode;

    @NotBlank
    @Size(max = 64)
    private String cityName;

    @Size(max = 64)
    private String districtName;

    @NotNull
    private PetType petType;

    @Size(max = 64)
    private String petName;

    private PetGender petGender;

    @Min(0)
    @Max(360)
    private Integer ageMonths;

    @Size(max = 128)
    private String breed;

    @DecimalMin("0.00")
    @DecimalMax("99.99")
    private BigDecimal weightKg;

    private NeuteredStatus neuteredStatus;

    private VaccinatedStatus vaccinatedStatus;

    @Size(max = 1000)
    private String healthNote;

    @Size(max = 10)
    private List<@NotBlank @Size(max = 20) String> temperamentTags;

    @Size(max = 1000)
    private String specialCareNote;

    @NotEmpty
    @Size(min = 1, max = 9)
    private List<@NotNull Long> petImageFileIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public PetGender getPetGender() {
        return petGender;
    }

    public void setPetGender(PetGender petGender) {
        this.petGender = petGender;
    }

    public Integer getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(Integer ageMonths) {
        this.ageMonths = ageMonths;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public NeuteredStatus getNeuteredStatus() {
        return neuteredStatus;
    }

    public void setNeuteredStatus(NeuteredStatus neuteredStatus) {
        this.neuteredStatus = neuteredStatus;
    }

    public VaccinatedStatus getVaccinatedStatus() {
        return vaccinatedStatus;
    }

    public void setVaccinatedStatus(VaccinatedStatus vaccinatedStatus) {
        this.vaccinatedStatus = vaccinatedStatus;
    }

    public String getHealthNote() {
        return healthNote;
    }

    public void setHealthNote(String healthNote) {
        this.healthNote = healthNote;
    }

    public List<String> getTemperamentTags() {
        return temperamentTags;
    }

    public void setTemperamentTags(List<String> temperamentTags) {
        this.temperamentTags = temperamentTags;
    }

    public String getSpecialCareNote() {
        return specialCareNote;
    }

    public void setSpecialCareNote(String specialCareNote) {
        this.specialCareNote = specialCareNote;
    }

    public List<Long> getPetImageFileIds() {
        return petImageFileIds;
    }

    public void setPetImageFileIds(List<Long> petImageFileIds) {
        this.petImageFileIds = petImageFileIds;
    }
}

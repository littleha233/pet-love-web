package com.petlove.weblove.modules.adoption.entity;

import com.petlove.weblove.common.util.BaseEntity;
import com.petlove.weblove.modules.adoption.enums.NeuteredStatus;
import com.petlove.weblove.modules.adoption.enums.PetGender;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.enums.VaccinatedStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pets")
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", nullable = false)
    private PetType petType;

    private String name;

    @Enumerated(EnumType.STRING)
    private PetGender gender;

    @Column(name = "age_months")
    private Integer ageMonths;

    private String breed;

    @Column(name = "weight_kg")
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "neutered_status")
    private NeuteredStatus neuteredStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "vaccinated_status")
    private VaccinatedStatus vaccinatedStatus;

    @Column(name = "health_note", columnDefinition = "TEXT")
    private String healthNote;

    @Column(name = "temperament_tags", columnDefinition = "JSON")
    private String temperamentTags;

    @Column(name = "special_care_note", columnDefinition = "TEXT")
    private String specialCareNote;

    public Long getId() {
        return id;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PetGender getGender() {
        return gender;
    }

    public void setGender(PetGender gender) {
        this.gender = gender;
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

    public String getTemperamentTags() {
        return temperamentTags;
    }

    public void setTemperamentTags(String temperamentTags) {
        this.temperamentTags = temperamentTags;
    }

    public String getSpecialCareNote() {
        return specialCareNote;
    }

    public void setSpecialCareNote(String specialCareNote) {
        this.specialCareNote = specialCareNote;
    }
}

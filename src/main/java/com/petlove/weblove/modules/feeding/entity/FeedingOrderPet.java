package com.petlove.weblove.modules.feeding.entity;

import com.petlove.weblove.common.util.CreatedOnlyEntity;
import com.petlove.weblove.modules.adoption.enums.PetGender;
import com.petlove.weblove.modules.adoption.enums.PetType;
import jakarta.persistence.*;

@Entity
@Table(
    name = "feeding_order_pets",
    uniqueConstraints = @UniqueConstraint(name = "uk_feeding_order_pets_order_pet", columnNames = {"order_id", "pet_id"})
)
public class FeedingOrderPet extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "pet_name_snapshot")
    private String petNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type_snapshot", nullable = false)
    private PetType petTypeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_gender_snapshot")
    private PetGender petGenderSnapshot;

    @Column(name = "age_months_snapshot")
    private Integer ageMonthsSnapshot;

    @Column(name = "breed_snapshot")
    private String breedSnapshot;

    @Column(name = "special_care_note_snapshot", columnDefinition = "TEXT")
    private String specialCareNoteSnapshot;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetNameSnapshot() {
        return petNameSnapshot;
    }

    public void setPetNameSnapshot(String petNameSnapshot) {
        this.petNameSnapshot = petNameSnapshot;
    }

    public PetType getPetTypeSnapshot() {
        return petTypeSnapshot;
    }

    public void setPetTypeSnapshot(PetType petTypeSnapshot) {
        this.petTypeSnapshot = petTypeSnapshot;
    }

    public PetGender getPetGenderSnapshot() {
        return petGenderSnapshot;
    }

    public void setPetGenderSnapshot(PetGender petGenderSnapshot) {
        this.petGenderSnapshot = petGenderSnapshot;
    }

    public Integer getAgeMonthsSnapshot() {
        return ageMonthsSnapshot;
    }

    public void setAgeMonthsSnapshot(Integer ageMonthsSnapshot) {
        this.ageMonthsSnapshot = ageMonthsSnapshot;
    }

    public String getBreedSnapshot() {
        return breedSnapshot;
    }

    public void setBreedSnapshot(String breedSnapshot) {
        this.breedSnapshot = breedSnapshot;
    }

    public String getSpecialCareNoteSnapshot() {
        return specialCareNoteSnapshot;
    }

    public void setSpecialCareNoteSnapshot(String specialCareNoteSnapshot) {
        this.specialCareNoteSnapshot = specialCareNoteSnapshot;
    }
}

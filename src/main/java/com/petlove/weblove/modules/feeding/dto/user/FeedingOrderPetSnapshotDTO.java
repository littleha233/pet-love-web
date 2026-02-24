package com.petlove.weblove.modules.feeding.dto.user;

public record FeedingOrderPetSnapshotDTO(
    Long petId,
    String petName,
    String petType,
    String petGender,
    Integer ageMonths,
    String breed,
    String specialCareNote
) {
}

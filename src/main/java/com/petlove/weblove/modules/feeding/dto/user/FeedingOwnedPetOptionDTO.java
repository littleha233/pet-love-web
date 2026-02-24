package com.petlove.weblove.modules.feeding.dto.user;

public record FeedingOwnedPetOptionDTO(
    Long petId,
    String petName,
    String petType,
    String petGender,
    Integer ageMonths,
    String breed
) {
}

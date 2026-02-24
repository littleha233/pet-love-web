package com.petlove.weblove.modules.adoption.dto.user;

import java.math.BigDecimal;
import java.util.List;

public record RehomePostPetDTO(
    long petId,
    String petType,
    String name,
    String gender,
    Integer ageMonths,
    String breed,
    BigDecimal weightKg,
    String neuteredStatus,
    String vaccinatedStatus,
    String healthNote,
    List<String> temperamentTags,
    String specialCareNote,
    List<PetMediaDTO> media
) {
}

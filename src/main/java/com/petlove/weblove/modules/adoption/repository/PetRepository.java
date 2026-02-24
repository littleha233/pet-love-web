package com.petlove.weblove.modules.adoption.repository;

import com.petlove.weblove.modules.adoption.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}

package com.petlove.weblove.modules.adoption.repository;

import com.petlove.weblove.modules.adoption.entity.PetMedia;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetMediaRepository extends JpaRepository<PetMedia, Long> {

    List<PetMedia> findByPetIdOrderBySortOrderAsc(Long petId);

    List<PetMedia> findByPetIdInOrderByPetIdAscSortOrderAsc(Collection<Long> petIds);
}

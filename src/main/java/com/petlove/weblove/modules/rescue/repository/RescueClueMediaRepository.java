package com.petlove.weblove.modules.rescue.repository;

import com.petlove.weblove.modules.rescue.entity.RescueClueMedia;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RescueClueMediaRepository extends JpaRepository<RescueClueMedia, Long> {

    List<RescueClueMedia> findByClueIdOrderBySortOrderAsc(Long clueId);

    List<RescueClueMedia> findByClueIdInOrderByClueIdAscSortOrderAsc(Collection<Long> clueIds);
}

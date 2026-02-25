package com.petlove.weblove.modules.rescue.repository;

import com.petlove.weblove.modules.rescue.entity.RescueResource;
import com.petlove.weblove.modules.rescue.enums.RescueResourceStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RescueResourceRepository extends JpaRepository<RescueResource, Long>, JpaSpecificationExecutor<RescueResource> {

    Optional<RescueResource> findByIdAndStatus(Long id, RescueResourceStatus status);

    List<RescueResource> findByIdIn(Collection<Long> ids);
}

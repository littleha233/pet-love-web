package com.petlove.weblove.modules.rescue.repository;

import com.petlove.weblove.modules.rescue.entity.RescueGuide;
import com.petlove.weblove.modules.rescue.enums.RescueGuideStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RescueGuideRepository extends JpaRepository<RescueGuide, Long>, JpaSpecificationExecutor<RescueGuide> {

    Optional<RescueGuide> findByIdAndStatus(Long id, RescueGuideStatus status);
}

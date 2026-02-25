package com.petlove.weblove.modules.rescue.repository;

import com.petlove.weblove.modules.rescue.entity.RescueClue;
import com.petlove.weblove.modules.rescue.enums.RescueClueStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RescueClueRepository extends JpaRepository<RescueClue, Long>, JpaSpecificationExecutor<RescueClue> {

    boolean existsByClueNo(String clueNo);

    Page<RescueClue> findByReporterUserId(Long reporterUserId, Pageable pageable);

    Page<RescueClue> findByReporterUserIdAndStatus(Long reporterUserId, RescueClueStatus status, Pageable pageable);

    Optional<RescueClue> findByIdAndReporterUserId(Long id, Long reporterUserId);
}

package com.petlove.weblove.modules.adoption.repository;

import com.petlove.weblove.modules.adoption.entity.AdoptionApplication;
import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {

    Optional<AdoptionApplication> findByPostIdAndApplicantUserId(Long postId, Long applicantUserId);

    Page<AdoptionApplication> findByApplicantUserId(Long applicantUserId, Pageable pageable);

    Page<AdoptionApplication> findByApplicantUserIdAndStatus(Long applicantUserId,
                                                              AdoptionApplicationStatus status,
                                                              Pageable pageable);

    Page<AdoptionApplication> findByPostId(Long postId, Pageable pageable);

    long countByPostId(Long postId);

    long countByPostIdAndStatus(Long postId, AdoptionApplicationStatus status);

    List<AdoptionApplication> findByPostIdAndStatus(Long postId, AdoptionApplicationStatus status);

    List<AdoptionApplication> findByPostIdIn(Collection<Long> postIds);
}

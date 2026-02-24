package com.petlove.weblove.modules.adoption.repository;

import com.petlove.weblove.modules.adoption.entity.AdoptionPost;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdoptionPostRepository extends JpaRepository<AdoptionPost, Long>, JpaSpecificationExecutor<AdoptionPost> {

    Page<AdoptionPost> findByPublisherUserId(Long publisherUserId, Pageable pageable);

    Page<AdoptionPost> findByPublisherUserIdAndStatus(Long publisherUserId, AdoptionPostStatus status, Pageable pageable);

    List<AdoptionPost> findByPetIdIn(Collection<Long> petIds);
}

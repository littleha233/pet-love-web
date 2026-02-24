package com.petlove.weblove.modules.feeding.repository;

import com.petlove.weblove.modules.feeding.entity.FeedingProviderProfile;
import com.petlove.weblove.modules.feeding.enums.FeedingProviderProfileStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeedingProviderProfileRepository extends JpaRepository<FeedingProviderProfile, Long>,
    JpaSpecificationExecutor<FeedingProviderProfile> {

    Optional<FeedingProviderProfile> findByProviderUserId(Long providerUserId);

    Optional<FeedingProviderProfile> findByProviderUserIdAndStatus(Long providerUserId, FeedingProviderProfileStatus status);

    List<FeedingProviderProfile> findByProviderUserIdIn(Collection<Long> providerUserIds);
}

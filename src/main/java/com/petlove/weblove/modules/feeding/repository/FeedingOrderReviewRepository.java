package com.petlove.weblove.modules.feeding.repository;

import com.petlove.weblove.modules.feeding.entity.FeedingOrderReview;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedingOrderReviewRepository extends JpaRepository<FeedingOrderReview, Long> {

    Optional<FeedingOrderReview> findByOrderId(Long orderId);

    List<FeedingOrderReview> findByOrderIdIn(Collection<Long> orderIds);

    boolean existsByOrderId(Long orderId);
}

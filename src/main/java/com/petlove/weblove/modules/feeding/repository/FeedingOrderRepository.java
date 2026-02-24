package com.petlove.weblove.modules.feeding.repository;

import com.petlove.weblove.modules.feeding.entity.FeedingOrder;
import com.petlove.weblove.modules.feeding.enums.FeedingOrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedingOrderRepository extends JpaRepository<FeedingOrder, Long> {

    boolean existsByOrderNo(String orderNo);

    Page<FeedingOrder> findByOwnerUserId(Long ownerUserId, Pageable pageable);

    Page<FeedingOrder> findByOwnerUserIdAndStatus(Long ownerUserId, FeedingOrderStatus status, Pageable pageable);

    Page<FeedingOrder> findByProviderUserId(Long providerUserId, Pageable pageable);

    Page<FeedingOrder> findByProviderUserIdAndStatus(Long providerUserId, FeedingOrderStatus status, Pageable pageable);

    List<FeedingOrder> findByIdIn(Collection<Long> ids);
}

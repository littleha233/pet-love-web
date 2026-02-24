package com.petlove.weblove.modules.feeding.repository;

import com.petlove.weblove.modules.feeding.entity.FeedingOrderPet;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedingOrderPetRepository extends JpaRepository<FeedingOrderPet, Long> {

    List<FeedingOrderPet> findByOrderIdOrderByIdAsc(Long orderId);

    List<FeedingOrderPet> findByOrderIdInOrderByOrderIdAscIdAsc(Collection<Long> orderIds);
}

package com.petlove.weblove.modules.feeding.repository;

import com.petlove.weblove.modules.feeding.entity.FeedingVisitMedia;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedingVisitMediaRepository extends JpaRepository<FeedingVisitMedia, Long> {

    List<FeedingVisitMedia> findByVisitIdOrderBySortOrderAsc(Long visitId);

    List<FeedingVisitMedia> findByVisitIdInOrderByVisitIdAscSortOrderAsc(Collection<Long> visitIds);
}

package com.petlove.weblove.modules.feeding.repository;

import com.petlove.weblove.modules.feeding.entity.FeedingOrderVisit;
import com.petlove.weblove.modules.feeding.enums.FeedingVisitStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedingOrderVisitRepository extends JpaRepository<FeedingOrderVisit, Long> {

    List<FeedingOrderVisit> findByOrderIdOrderByVisitIndexAsc(Long orderId);

    List<FeedingOrderVisit> findByOrderIdInOrderByOrderIdAscVisitIndexAsc(Collection<Long> orderIds);

    boolean existsByOrderIdAndStatus(Long orderId, FeedingVisitStatus status);

    boolean existsByOrderIdAndStatusIn(Long orderId, Collection<FeedingVisitStatus> statuses);

    @Query("""
        select min(v.plannedStartAt) from FeedingOrderVisit v
        where v.orderId = :orderId and v.status in :statuses
        """)
    Optional<LocalDateTime> findNextPlannedStartAt(@Param("orderId") Long orderId,
                                                   @Param("statuses") Collection<FeedingVisitStatus> statuses);
}

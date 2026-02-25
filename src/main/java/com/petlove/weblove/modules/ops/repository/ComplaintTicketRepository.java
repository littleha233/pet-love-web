package com.petlove.weblove.modules.ops.repository;

import com.petlove.weblove.modules.ops.entity.ComplaintTicket;
import com.petlove.weblove.modules.ops.enums.ComplaintTicketStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComplaintTicketRepository extends JpaRepository<ComplaintTicket, Long>, JpaSpecificationExecutor<ComplaintTicket> {

    boolean existsByTicketNo(String ticketNo);

    Page<ComplaintTicket> findByReporterUserId(Long reporterUserId, Pageable pageable);

    Page<ComplaintTicket> findByReporterUserIdAndStatus(Long reporterUserId, ComplaintTicketStatus status, Pageable pageable);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countByStatusIn(Collection<ComplaintTicketStatus> statuses);
}

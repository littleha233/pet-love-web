package com.petlove.weblove.modules.ops.repository;

import com.petlove.weblove.modules.ops.entity.ComplaintTicketReply;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintTicketReplyRepository extends JpaRepository<ComplaintTicketReply, Long> {

    List<ComplaintTicketReply> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);

    List<ComplaintTicketReply> findByTicketIdInOrderByTicketIdAscCreatedAtAscIdAsc(Collection<Long> ticketIds);
}

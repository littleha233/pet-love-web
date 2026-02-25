package com.petlove.weblove.modules.ops.dto.admin;

import java.time.LocalDateTime;

public record AdminComplaintReplyDTO(
    Long replyId,
    String authorType,
    String authorName,
    String content,
    Boolean isInternalNote,
    LocalDateTime createdAt
) {
}

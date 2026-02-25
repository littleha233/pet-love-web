package com.petlove.weblove.modules.ops.dto.user;

import java.time.LocalDateTime;

public record ComplaintReplyDTO(
    Long replyId,
    String authorType,
    String authorName,
    String content,
    LocalDateTime createdAt
) {
}

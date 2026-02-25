package com.petlove.weblove.modules.ops.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record ComplaintTicketDetailDTO(
    Long ticketId,
    String ticketNo,
    String targetType,
    Long targetId,
    String title,
    String content,
    String priority,
    String status,
    String triageNote,
    String resolutionNote,
    String contactMobileMasked,
    List<ComplaintEvidencePhotoDTO> evidencePhotos,
    List<ComplaintReplyDTO> replies,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

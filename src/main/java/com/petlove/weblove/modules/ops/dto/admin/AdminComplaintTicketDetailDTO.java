package com.petlove.weblove.modules.ops.dto.admin;

import com.petlove.weblove.modules.ops.dto.user.ComplaintEvidencePhotoDTO;
import java.time.LocalDateTime;
import java.util.List;

public record AdminComplaintTicketDetailDTO(
    Long ticketId,
    String ticketNo,
    Long reporterUserId,
    String reporterNickname,
    String targetType,
    Long targetId,
    String title,
    String content,
    String priority,
    String status,
    Long assignedAdminId,
    String assignedAdminName,
    String triageNote,
    String resolutionNote,
    String contactMobile,
    List<ComplaintEvidencePhotoDTO> evidencePhotos,
    List<AdminComplaintReplyDTO> replies,
    LocalDateTime lastReplyAt,
    LocalDateTime handledAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

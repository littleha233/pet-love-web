package com.petlove.weblove.modules.feeding.dto.provider;

import com.petlove.weblove.modules.feeding.dto.user.VisitPhotoDTO;
import java.time.LocalDateTime;
import java.util.List;

public record FeedingVisitLogDTO(
    Long visitId,
    String status,
    LocalDateTime actualStartAt,
    LocalDateTime actualEndAt,
    boolean foodDone,
    boolean waterDone,
    boolean litterDone,
    boolean playDone,
    String healthObservation,
    String visitNote,
    List<VisitPhotoDTO> photos
) {
}

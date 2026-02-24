package com.petlove.weblove.modules.feeding.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public record FeedingOrderVisitDTO(
    Long visitId,
    Integer visitIndex,
    LocalDateTime plannedStartAt,
    LocalDateTime plannedEndAt,
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

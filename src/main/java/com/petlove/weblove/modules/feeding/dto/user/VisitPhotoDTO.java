package com.petlove.weblove.modules.feeding.dto.user;

public record VisitPhotoDTO(
    Long fileId,
    String url,
    Integer sortOrder
) {
}

package com.petlove.weblove.modules.file.dto;

public record FileObjectDTO(
    long fileId,
    Long ownerUserId,
    String bizType,
    String url,
    String mimeType,
    long fileSize,
    String status
) {
}

package com.petlove.weblove.modules.file.dto;

public record FileUploadResponse(
    long fileId,
    String url,
    String fileName,
    String mimeType,
    long fileSize,
    String status
) {
}

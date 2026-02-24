package com.petlove.weblove.modules.file.service;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.HashUtil;
import com.petlove.weblove.config.AppStorageProperties;
import com.petlove.weblove.modules.file.dto.FileObjectDTO;
import com.petlove.weblove.modules.file.dto.FileUploadResponse;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.security.SecurityUtils;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private static final Set<String> AVATAR_ALLOWED_MIME_TYPES = Set.of(
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE,
        "image/webp"
    );

    private final FileObjectRepository fileObjectRepository;
    private final LocalStorageService localStorageService;
    private final AppStorageProperties storageProperties;

    public FileService(FileObjectRepository fileObjectRepository,
                       LocalStorageService localStorageService,
                       AppStorageProperties storageProperties) {
        this.fileObjectRepository = fileObjectRepository;
        this.localStorageService = localStorageService;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public FileUploadResponse upload(MultipartFile file, FileBizType bizType) {
        if (file.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "File cannot be empty");
        }
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new BizException(ErrorCode.FILE_TOO_LARGE, "File is too large");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "MIME type is missing");
        }

        if (bizType == FileBizType.AVATAR && !AVATAR_ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BizException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "Avatar file type is not allowed");
        }

        long userId = SecurityUtils.currentUserId();
        String extension = extractExtension(file.getOriginalFilename());
        String objectKey = LocalDate.now() + "/" + UUID.randomUUID() + extension;

        FileObject fileObject = new FileObject();
        fileObject.setOwnerUserId(userId);
        fileObject.setBucket("local");
        fileObject.setObjectKey(objectKey);
        fileObject.setBizType(bizType);
        fileObject.setFileName(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        fileObject.setMimeType(contentType);
        fileObject.setFileSize(file.getSize());
        fileObject.setStatus(FileStatus.UPLOADING);
        fileObject = fileObjectRepository.save(fileObject);

        try {
            localStorageService.save(objectKey, file);
            fileObject.setStatus(FileStatus.READY);
            fileObject.setPublicUrl(storageProperties.getBaseUrl() + "/" + fileObject.getId());
            fileObject.setSha256(HashUtil.sha256(file.getBytes()));
            fileObject = fileObjectRepository.save(fileObject);
        } catch (IOException e) {
            fileObject.setStatus(FileStatus.FAILED);
            fileObjectRepository.save(fileObject);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to store file");
        }

        return new FileUploadResponse(
            fileObject.getId(),
            fileObject.getPublicUrl(),
            fileObject.getFileName(),
            fileObject.getMimeType(),
            fileObject.getFileSize(),
            fileObject.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public FileObjectDTO getMeta(Long fileId) {
        FileObject fileObject = fileObjectRepository.findById(fileId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "File object not found"));
        return new FileObjectDTO(
            fileObject.getId(),
            fileObject.getOwnerUserId(),
            fileObject.getBizType().name(),
            fileObject.getPublicUrl(),
            fileObject.getMimeType(),
            fileObject.getFileSize(),
            fileObject.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> loadContent(Long fileId) {
        FileObject fileObject = fileObjectRepository.findById(fileId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "File object not found"));

        if (fileObject.getStatus() != FileStatus.READY) {
            throw new BizException(ErrorCode.FILE_NOT_READY, "File is not ready");
        }

        Resource resource = localStorageService.load(fileObject.getObjectKey());
        if (!resource.exists()) {
            throw new BizException(ErrorCode.NOT_FOUND, "File content not found");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(fileObject.getMimeType()))
            .body(resource);
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) {
            return "";
        }
        return fileName.substring(idx);
    }
}

package com.petlove.weblove.modules.file.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.modules.file.dto.FileObjectDTO;
import com.petlove.weblove.modules.file.dto.FileUploadResponse;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("bizType") FileBizType bizType) {
        return ApiResponse.success(fileService.upload(file, bizType));
    }

    @GetMapping("/{fileId}")
    public ApiResponse<FileObjectDTO> getMeta(@PathVariable Long fileId) {
        return ApiResponse.success(fileService.getMeta(fileId));
    }

    @GetMapping("/content/{fileId}")
    public ResponseEntity<Resource> getContent(@PathVariable Long fileId) {
        return fileService.loadContent(fileId);
    }
}

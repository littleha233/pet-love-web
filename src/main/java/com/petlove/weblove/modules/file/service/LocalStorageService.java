package com.petlove.weblove.modules.file.service;

import com.petlove.weblove.config.AppStorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService {

    private final AppStorageProperties storageProperties;

    public LocalStorageService(AppStorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public void save(String objectKey, MultipartFile file) throws IOException {
        Path rootPath = Paths.get(storageProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(objectKey).normalize();
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath);
    }

    public Resource load(String objectKey) {
        Path rootPath = Paths.get(storageProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(objectKey).normalize();
        return new FileSystemResource(targetPath);
    }

    public boolean exists(String objectKey) {
        Path rootPath = Paths.get(storageProperties.getLocalRoot()).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(objectKey).normalize();
        return Files.exists(targetPath);
    }
}

package com.studentsupport.service;

import com.studentsupport.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir;

    public FileStorageService(@Value("${app.upload.dir:uploads/chat}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + rootDir, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String storedName = UUID.randomUUID() + extension;

        try {
            Path target = rootDir.resolve(storedName).normalize();
            if (!target.getParent().equals(rootDir)) {
                throw new BadRequestException("Invalid file name");
            }
            Files.copy(file.getInputStream(), target);
            return storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file " + originalName, e);
        }
    }

    public void delete(String storedName) {
        if (storedName == null) {
            return;
        }
        try {
            Path target = rootDir.resolve(storedName).normalize();
            if (!target.getParent().equals(rootDir)) {
                return;
            }
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    public Resource loadAsResource(String storedName) {
        try {
            Path filePath = rootDir.resolve(storedName).normalize();
            if (!filePath.getParent().equals(rootDir)) {
                throw new BadRequestException("Invalid file name");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("File not found: " + storedName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BadRequestException("File not found: " + storedName);
        }
    }
}

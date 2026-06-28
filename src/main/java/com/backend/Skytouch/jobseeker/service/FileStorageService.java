package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.common.config.StorageProperties;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final StorageProperties storageProperties;
    private final ObjectProvider<Cloudinary> cloudinaryProvider;

    public String uploadPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CV file is required");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        if (storageProperties.isCloudinaryEnabled()) {
            return uploadToCloudinary(file);
        }
        return saveLocally(file);
    }

    private String uploadToCloudinary(MultipartFile file) {
        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new BadRequestException("Cloudinary is not configured");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "jobseekers/cv"
                    )
            );
            return result.get("secure_url").toString();
        } catch (Exception ex) {
            log.error("Cloudinary upload failed", ex);
            throw new BadRequestException("CV upload failed: " + ex.getMessage());
        }
    }

    private String saveLocally(MultipartFile file) {
        try {
            Path cvDir = Path.of(storageProperties.getLocalUploadDir(), "jobseekers", "cv")
                    .toAbsolutePath();
            Files.createDirectories(cvDir);

            String filename = UUID.randomUUID() + ".pdf";
            Path destination = cvDir.resolve(filename);
            file.transferTo(destination);

            return "/uploads/jobseekers/cv/" + filename;
        } catch (IOException ex) {
            log.error("Local CV upload failed", ex);
            throw new BadRequestException("CV upload failed: " + ex.getMessage());
        }
    }
}

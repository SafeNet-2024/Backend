package com.SafeNet.Backend.domain.file.service;

import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.file.domain.FileType;
import com.SafeNet.Backend.domain.file.exception.FileStorageException;
import com.SafeNet.Backend.domain.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    // @Value를 사용한 명시적 생성자 정의
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir, FileRepository fileRepository) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.fileRepository = fileRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    public File saveFile(MultipartFile file, FileType fileType) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Empty file cannot be saved.");
        }
        try {
            // 파일 이름 생성
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // File 엔티티 생성 및 저장
            File files = File.builder()
                    .fileType(fileType)
                    .fileUrl(targetLocation.toString())
                    .build();

            return fileRepository.save(files);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }
}

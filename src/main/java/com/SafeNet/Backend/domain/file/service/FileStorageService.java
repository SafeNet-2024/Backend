package com.SafeNet.Backend.domain.file.service;

import com.SafeNet.Backend.domain.file.domain.File;
import com.SafeNet.Backend.domain.file.domain.FileType;
import com.SafeNet.Backend.domain.file.exception.FileStorageException;
import com.SafeNet.Backend.domain.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileStorageService {
    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    public File saveFile(MultipartFile file, FileType fileType) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Empty file cannot be saved.");
        }
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 파일 이름 생성
            Path targetLocation = this.fileStorageLocation.resolve(fileName); // 타겟 파일 경로 설정
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING); // 입력 스트림의 데이터를 타겟 파일 경로로 복사, 해당 파일이 존재한다면 덮어쓰기

            // Files 엔티티 생성 및 저장
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

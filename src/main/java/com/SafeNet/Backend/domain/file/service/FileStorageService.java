package com.SafeNet.Backend.domain.file.service;

import com.SafeNet.Backend.domain.file.entity.File;
import com.SafeNet.Backend.domain.file.entity.FileType;
import com.SafeNet.Backend.domain.file.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileStorageService {

    private final FileRepository fileRepository;

    public FileStorageService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // TODO 유저 정보 넣어서 파일에 저장하기
    @Transactional
    public File saveFile(String fileUrl, FileType fileType) {
        File files = File.builder()
                .fileType(fileType)
                .fileUrl(fileUrl)
                .build();
        return fileRepository.save(files);
    }

    @Transactional
    public void deleteFile(File file) {
        fileRepository.delete(file);
    }
}

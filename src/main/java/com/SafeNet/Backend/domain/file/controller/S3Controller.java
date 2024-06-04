package com.SafeNet.Backend.domain.file.controller;

import com.SafeNet.Backend.domain.file.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
public class S3Controller {
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping(path = "/s3/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestPart(value = "receiptImage") MultipartFile receiptImage
    ) throws IOException {
        String receipt_fileName = receiptImage.getOriginalFilename();
        String receiptUrl = s3Service.upload("receiptImage", receipt_fileName, receiptImage);
        log.info("receipt_url is : " + receiptUrl);
        return new ResponseEntity<>("Receipt URL: " + receiptUrl, HttpStatus.OK);
    }

    @DeleteMapping(path = "/s3/test")
    public ResponseEntity<String> deleteFile(
            @RequestParam(value = "fileUrl") String fileUrl
    ) {
        try {
            // S3Service를 사용하여 파일 삭제
            s3Service.delete(fileUrl);
            // 삭제 성공 메시지 반환
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            // 삭제 실패 시 예외 처리
            log.error("Failed to delete file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }
    }
}

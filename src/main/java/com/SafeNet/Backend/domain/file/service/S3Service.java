package com.SafeNet.Backend.domain.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String upload(String dirName, String fileName, MultipartFile multipartFile) throws IOException {
        try {
            File uploadFile = convert(multipartFile)
                    .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패: 파일 변환 중 예외 발생"));
            return upload(dirName, fileName, uploadFile);
        } catch (RuntimeException e) {
            log.error("알 수 없는 예외 발생: 파일 업로드 실패. 디렉토리 이름: {}, 파일 이름: {}, 원본 파일 이름: {}, 에러 메시지: {}",
                    dirName, fileName, multipartFile.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("파일 업로드 중 알 수 없는 예외 발생. 디렉토리 이름: " + dirName +
                    ", 파일 이름: " + fileName + ", 원본 파일 이름: " + multipartFile.getOriginalFilename() +
                    ", 에러 메시지: " + e.getMessage(), e);
        }
    }

    private String upload(String dirName, String fileName, File uploadFile) {
        String uniqueFileName = generateUniqueFilename(fileName);
        String newFileName = dirName + "/" + uniqueFileName;
        String uploadImageUrl = putS3(uploadFile, newFileName);
        removeNewFile(uploadFile);  // MultipartFile -> File 전환함으로 인해 로컬에 생성된 File 삭제
        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)    // PublicRead 권한으로 업로드 됨
        );
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public void delete(String fileUrl) {
        String fileKey = extractFileKey(fileUrl);
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        log.info(file.getOriginalFilename());
        File convertFile = new File(Objects.requireNonNull(file.getOriginalFilename())); // 업로드한 파일의 이름
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes()); // 실제 로컬 파일에 저장
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    private String generateUniqueFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
            originalFilename = originalFilename.substring(0, dotIndex);
        }
        return originalFilename + "_" + uuid + extension;
    }

    private String extractFileKey(String fileUrl) {
        // S3 버킷 URL 부분 제거하여 파일 키 추출
        String fileKey = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1); // 버킷 이름을 제외한 나머지 부분을 파일 키로 추출
        return URLDecoder.decode(fileKey, StandardCharsets.UTF_8);
    }
}
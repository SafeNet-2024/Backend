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
            @RequestPart(value = "receiptImage") MultipartFile receiptImage,
            @RequestPart(value = "productImage") MultipartFile productImage
    ) throws IOException {
        String receipt_fileName = receiptImage.getOriginalFilename();
        String product_fileName = productImage.getOriginalFilename();

        String receiptUrl = s3Service.upload("receiptImage", receipt_fileName, receiptImage);
        String productUrl = s3Service.upload("productImage", product_fileName, productImage);
        log.info("receipt_url is : " + receiptUrl);
        log.info("product_url is : " + productUrl);
        return new ResponseEntity<>("Receipt URL: " + receiptUrl + "\nProduct URL: " + productUrl, HttpStatus.OK);
    }
}

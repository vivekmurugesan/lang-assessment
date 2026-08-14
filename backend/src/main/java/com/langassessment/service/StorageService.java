package com.langassessment.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.assessments}")
    private String assessmentsBucket;

    @Value("${minio.bucket.submissions}")
    private String submissionsBucket;

    public void initializeBuckets() {
        try {
            createBucketIfNotExists(assessmentsBucket);
            createBucketIfNotExists(submissionsBucket);
            log.info("MinIO buckets initialized");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO buckets: {}", e.getMessage());
        }
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Created MinIO bucket: {}", bucketName);
        }
    }

    public String uploadFile(MultipartFile file, String bucketType) throws Exception {
        String bucketName = bucketType.equals("assessment") ? assessmentsBucket : submissionsBucket;
        String fileName = generateFileName(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("File uploaded successfully: {}", fileName);
            return fileName;
        }
    }

    public InputStream downloadFile(String fileName, String bucketType) throws Exception {
        String bucketName = bucketType.equals("assessment") ? assessmentsBucket : submissionsBucket;

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    public void deleteFile(String fileName, String bucketType) throws Exception {
        String bucketName = bucketType.equals("assessment") ? assessmentsBucket : submissionsBucket;

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted successfully: {}", fileName);
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.warn("File not found: {}", fileName);
            } else {
                throw e;
            }
        }
    }

    public String generatePresignedUrl(String fileName, String bucketType) throws Exception {
        String bucketName = bucketType.equals("assessment") ? assessmentsBucket : submissionsBucket;

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.GET)
                        .bucket(bucketName)
                        .object(fileName)
                        .expiration(60 * 60)
                        .build()
        );
    }

    private String generateFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }
}

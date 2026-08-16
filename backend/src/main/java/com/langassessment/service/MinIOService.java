package com.langassessment.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinIOService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.assessments:assessments}")
    private String assessmentsBucket;

    @Value("${minio.bucket.questions:questions}")
    private String questionsBucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    public String uploadAudio(byte[] audioData, String fileName) {
        try {
            String objectName = "audio/" + UUID.randomUUID() + "_" + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(questionsBucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(audioData), audioData.length, -1)
                            .contentType("audio/wav")
                            .build()
            );
            log.info("Audio uploaded to MinIO: {}", objectName);
            return getObjectUrl(questionsBucket, objectName);
        } catch (Exception e) {
            log.error("Failed to upload audio to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload audio: " + e.getMessage());
        }
    }

    public String uploadImage(byte[] imageData, String fileName, String contentType) {
        try {
            String objectName = "images/" + UUID.randomUUID() + "_" + fileName;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(questionsBucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(imageData), imageData.length, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Image uploaded to MinIO: {}", objectName);
            return getObjectUrl(questionsBucket, objectName);
        } catch (Exception e) {
            log.error("Failed to upload image to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    public InputStream downloadObject(String bucket, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download object from MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download object: " + e.getMessage());
        }
    }

    public void deleteObject(String bucket, String objectUrl) {
        try {
            String objectName = extractObjectNameFromUrl(objectUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("Object deleted from MinIO: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete object from MinIO: {}", e.getMessage(), e);
        }
    }

    public String uploadFile(String bucket, String objectName, InputStream inputStream, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType("application/json")
                            .build()
            );
            log.info("File uploaded to MinIO: {}/{}", bucket, objectName);
            return getObjectUrl(bucket, objectName);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public String getObjectUrl(String bucket, String objectName) {
        return String.format("%s/%s/%s", minioEndpoint.replaceAll("/$", ""), bucket, objectName);
    }

    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String[] parts = url.split("/");
        if (parts.length < 2) {
            return url;
        }
        return String.join("/", java.util.Arrays.copyOfRange(parts, parts.length - 2, parts.length));
    }
}

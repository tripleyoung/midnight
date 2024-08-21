package com.outsider.midnight.file.command.application.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;
    private static final char separator = '_';
    public boolean doesFileExist(String filename) throws Exception {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            // 파일이 존재하지 않을 때

            throw e;
        }
    }

    public String uploadFile(MultipartFile file) throws Exception {
        // 버킷이 존재하는지 확인하고, 존재하지 않으면 생성
        StringBuilder sb = new StringBuilder();

        sb.append(UUID.randomUUID());
        // uuid와 실제 파일이름 분리용문자
        sb.append(separator);
        sb.append(file.getOriginalFilename());

        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // 파일 업로드
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(sb.toString()).stream(
                                file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
        return sb.toString();
    }

    public String getFileUrl(String filename) throws Exception {
        try {
            // 객체가 존재하는지 확인
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            // 객체가 존재하면 프리사인드 URL 생성
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filename)
                            .expiry(24 * 60 * 60) // 24시간 유효
                            .build()
            );
        } catch (ErrorResponseException e) {

                // 다른 MinIO 관련 오류 처리
                throw new RuntimeException("Error accessing MinIO: " + e.getMessage());
        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }
}

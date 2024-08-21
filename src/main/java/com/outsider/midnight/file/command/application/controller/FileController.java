package com.outsider.midnight.file.command.application.controller;

import com.outsider.midnight.file.command.application.dto.UploadFileResponse;
import com.outsider.midnight.file.command.application.dto.UploadResponse;
import com.outsider.midnight.file.command.application.service.MinioService;
import com.outsider.midnight.util.FileUtil;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.annotation.MultipartConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Autowired
    private MinioService minioService;

    @Operation(summary = "음성 파일 업로드", description = "이미지 파일을 테스트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content)
    })
    @PostMapping(value = "/upload-file", consumes = "multipart/form-data")
    public ResponseEntity<UploadFileResponse> uploadFile2(
            @Parameter(description = "업로드할 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("file") MultipartFile  file) {
        try {
            System.out.println(file.getOriginalFilename());
            // Upload file and get the URL
            String fileNames = minioService.uploadFile(file);
            // Populate response fields
            String returnCode = "200"; // or appropriate status code
            String returnMsg = "File uploaded successfully";
            String imgTitle = fileNames; // or any other logic to get image title
            // Create response object
            UploadFileResponse response = new UploadFileResponse(returnCode, returnMsg, imgTitle);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // In case of an error, create a response object with error details
            UploadFileResponse errorResponse = new UploadFileResponse(
                    "500",
                    "Error uploading file.",
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<UploadResponse> uploadFile(
            @Parameter(description = "업로드할 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("imgData") MultipartFile  file) {
        try {
            System.out.println(file.getOriginalFilename());

            // Upload file and get the URL
            String fileNames = minioService.uploadFile(file);

            // Populate response fields
            String returnCode = "200"; // or appropriate status code
            String returnMsg = "File uploaded successfully";
            String imgTitle = FileUtil.generateFileName(file) ; // or any other logic to get image title
            String recogMsg = "Recognition content here"; // replace with actual recognition content if needed
            String mnStatus = "Normal"; // replace with actual mental status if needed

            // Create response object
            UploadResponse response = new UploadResponse(returnCode, returnMsg, imgTitle, recogMsg);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // In case of an error, create a response object with error details
            UploadResponse errorResponse = new UploadResponse(
                    "500",
                    "Error uploading file.",
                    null,
                    null
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @Hidden
    @GetMapping("/url")
    public ResponseEntity<String> getFileUrl(@RequestParam("filename") String filename) {
        try {
            // 파일이 실제로 존재하는지 확인
            boolean doesFileExist = minioService.doesFileExist(filename);
            if (!doesFileExist) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
            }

            String url = minioService.getFileUrl(filename);
            return ResponseEntity.ok(url);
        } catch (MinioException e) {
            // MinIO에서 발생하는 특정 예외 처리
            String errorMessage = "Error accessing MinIO: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        } catch (Exception e) {
            // 그 외 예외 처리
            String errorMessage = "Error generating file URL: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}

package com.outsider.midnight.file.command.application.controller;

import com.outsider.midnight.file.command.application.dto.UploadResponse;
import com.outsider.midnight.file.command.application.service.MinioService;
import jakarta.servlet.annotation.MultipartConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Autowired
    private MinioService minioService;

    @PostMapping("/upload2")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println(file.getOriginalFilename());

            // Upload file and get the URL
            String fileNames = minioService.uploadFile(file);

            // Populate response fields
            String returnCode = "200"; // or appropriate status code
            String returnMsg = "File uploaded successfully";
            String imgTitle = file.getOriginalFilename(); // or any other logic to get image title
            String recogMsg = "Recognition content here"; // replace with actual recognition content if needed
            String mnStatus = "Normal"; // replace with actual mental status if needed

            // Create response object
            UploadResponse response = new UploadResponse(returnCode, returnMsg, imgTitle, recogMsg, mnStatus);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // In case of an error, create a response object with error details
            UploadResponse errorResponse = new UploadResponse(
                    "500",
                    "Error uploading file.",
                    null,
                    null,
                    null
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping("/url")
    public ResponseEntity<String> getFileUrl(@RequestParam("filename") String filename) {
        try {
            String url = minioService.getFileUrl(filename);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error generating file URL.");
        }
    }
}

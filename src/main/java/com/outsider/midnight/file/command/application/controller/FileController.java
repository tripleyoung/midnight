package com.outsider.midnight.file.command.application.controller;

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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Upload file and get the URL
            String fileNames = minioService.uploadFile(file);
            return ResponseEntity.ok(fileNames);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file.");
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

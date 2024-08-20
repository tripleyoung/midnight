package com.outsider.midnight.file.command.application.controller;

import com.outsider.midnight.file.command.application.dto.AnalysisRequest;
import com.outsider.midnight.file.command.application.dto.AnalysisResponse;
import com.outsider.midnight.file.command.application.dto.UploadResponse;
import com.outsider.midnight.file.command.application.service.AIService;
import com.outsider.midnight.file.command.application.service.MinioService;
import com.outsider.midnight.file.command.domain.aggregate.AnalysisResult;
import com.outsider.midnight.file.command.domain.repository.AnalysisResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.concurrent.CompletableFuture;
@EnableAsync
@RestController
@RequestMapping("/api/v1/ai")
public class AIContoller {

    private MinioService minioService;
    private AIService aiService;
    private RestClient restClient;
    private AnalysisResultRepository aiResultRepository;

    public AIContoller(MinioService minioService, AIService aiService, RestClient restClient, AnalysisResultRepository aiResultRepository) {
        this.minioService = minioService;
        this.aiService = aiService;
        this.restClient = restClient;
        this.aiResultRepository = aiResultRepository;
    }


    @PostMapping("/upload")
    public DeferredResult<ResponseEntity<UploadResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        DeferredResult<ResponseEntity<UploadResponse>> deferredResult = new DeferredResult<>();

        // 비동기적으로 파일 업로드 및 AI 분석 요청 처리
        CompletableFuture.runAsync(() -> {
            try {
                // 파일 업로드 처리
                String fileNames = minioService.uploadFile(file);
                String url = minioService.getFileUrl(fileNames);

                // 분석 요청 객체 생성
                AnalysisRequest analysisRequest = new AnalysisRequest();
                analysisRequest.setImageUrl(url);

                // RestClient를 사용하여 AI 분석 서비스 호출 (동기)
                RestClient restClient = RestClient.builder().build();

                AnalysisResponse analysisResponse = restClient.post()
                        .uri("http://172.16.17.203:8000/analyze")  // AI 분석 서비스의 URL
                        .body(analysisRequest)
                        .retrieve()
                        .body(AnalysisResponse.class);

                AnalysisResult analysisResult = new AnalysisResult();
                analysisResult.setImageUrl(url);
                analysisResult.setAnalysis(analysisResponse.getAnalysis());
                analysisResult.setImageName(fileNames);
                analysisResult.setMnStatus("analysisResponse.getMnStatus()");
                aiResultRepository.save(analysisResult);

                // 응답 생성
                UploadResponse response = new UploadResponse(
                        "200",
                        "File uploaded and analyzed successfully",
                        file.getOriginalFilename(),
                        analysisResponse.getAnalysis(),
                        "analysisResponse.getMnStatus()"
                );

                deferredResult.setResult(ResponseEntity.ok(response));
            } catch (Exception e) {
                deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new UploadResponse("500", e.getMessage(), null, null, null)));
            }
        });

        return deferredResult;
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

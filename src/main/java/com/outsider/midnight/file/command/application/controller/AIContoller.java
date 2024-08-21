package com.outsider.midnight.file.command.application.controller;

import com.outsider.midnight.file.command.application.dto.*;
import com.outsider.midnight.file.command.application.service.AIService;
import com.outsider.midnight.file.command.application.service.MinioService;
import com.outsider.midnight.file.command.domain.aggregate.AnalysisResult;
import com.outsider.midnight.file.command.domain.repository.AnalysisResultRepository;
import com.outsider.midnight.util.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@EnableAsync
@RestController
@RequestMapping("/api/v1/ai")
public class AIContoller {

    private MinioService minioService;
    private AIService aiService;
    private RestClient restClient;
    private AnalysisResultRepository aiResultRepository;
    private final WebClient webClient;
    public AIContoller(MinioService minioService, AIService aiService, RestClient restClient, AnalysisResultRepository aiResultRepository, WebClient.Builder webClientBuilder) {
        this.minioService = minioService;
        this.aiService = aiService;
        this.restClient = restClient;
        this.aiResultRepository = aiResultRepository;
        this.webClient = webClientBuilder.build();

    }
    @Value("${ai.server.url}")
    public String aiServerUrl;


    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public DeferredResult<ResponseEntity<UploadResponse>> uploadFile(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("imgData") MultipartFile file) {
        DeferredResult<ResponseEntity<UploadResponse>> deferredResult = new DeferredResult<>();

        // WebClient 생성
        WebClient webClient = WebClient.builder().build();

        CompletableFuture.runAsync(() -> {
            try {
                // 파일 업로드 처리
                String fileNames = minioService.uploadFile(file);
                String url = minioService.getFileUrl(fileNames);

                // 분석 요청 객체 생성
                AnalysisRequest analysisRequest = new AnalysisRequest();
                analysisRequest.setImageUrl(url);

                // WebClient를 사용하여 AI 분석 서비스 호출 (비동기)
                webClient.post()
                        .uri(aiServerUrl)  // AI 분석 서비스의 URL
                        .bodyValue(analysisRequest)
                        .retrieve()
                        .bodyToMono(AnalysisResponse.class)
                        .doOnNext(analysisResponse -> {
                            // 분석 결과 저장
                            AnalysisResult analysisResult = new AnalysisResult();
                            analysisResult.setImageUrl(url);
                            analysisResult.setAnalysis(analysisResponse.getAnalysis());
                            analysisResult.setImageName(fileNames);
                            aiResultRepository.save(analysisResult);

                            // 성공 응답 생성
                            UploadResponse response = new UploadResponse(
                                    "200",
                                    "File uploaded and analyzed successfully",
                                    file.getOriginalFilename(),
                                    analysisResponse.getAnalysis()
                            );
                            deferredResult.setResult(ResponseEntity.ok(response));
                        })
                        .doOnError(e -> {
                            deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(new UploadResponse("500", e.getMessage(), null, null)));
                        })
                        .subscribe();
            } catch (Exception e) {
                deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new UploadResponse("500", e.getMessage(), null, null)));
            }
        });

        return deferredResult;
    }
}

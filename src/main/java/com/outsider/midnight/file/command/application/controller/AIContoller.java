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
    @PostMapping(value = "/upload-then-response-with-voice2", consumes = "multipart/form-data")
    public ResponseEntity<UploadAndResponseWithVoiceDTO> uploadFile4(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("imgData") MultipartFile file) {

        try {
            // 파일 업로드 처리
            String fileNames = minioService.uploadFile(file);
            String url = minioService.getFileUrl(fileNames);

            // 분석 요청 객체 생성
            AnalysisRequest analysisRequest = new AnalysisRequest();
            analysisRequest.setImageUrl(url);

            // WebClient를 사용하여 AI 분석 서비스 호출 (동기 방식)
            AnalysisResponseWithVoice analysisResponse = webClient.post()
                    .uri("http://172.16.17.203:8000/analyze_with_audio")
                    .bodyValue(analysisRequest)
                    .retrieve()
                    .bodyToMono(AnalysisResponseWithVoice.class)
                    .block(Duration.ofMinutes(5)); // 동기 방식으로 호출; // 동기 방식으로 호출

            if (analysisResponse == null) {
                throw new RuntimeException("AI 분석 서비스로부터 응답이 없습니다.");
            }

            // 분석 결과 저장 로직
            String ttsFileUrl;
            try {
                ttsFileUrl = minioService.getFileUrl(analysisResponse.getFileName());
            } catch (Exception e) {
                UploadAndResponseWithVoiceDTO errorResponse = new UploadAndResponseWithVoiceDTO("500", e.getMessage(), null, null, null);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // 성공 응답 생성
            UploadAndResponseWithVoiceDTO response = new UploadAndResponseWithVoiceDTO(
                    "200",
                    "File uploaded and analyzed successfully",
                    FileUtil.generateFileName(file),
                    analysisResponse.getAnalysis(), // 분석 결과
                    ttsFileUrl
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            UploadAndResponseWithVoiceDTO errorResponse = new UploadAndResponseWithVoiceDTO("300", e.getMessage(), null, null, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/upload3", consumes = "multipart/form-data")
    public ResponseEntity<UploadResponse> uploadFile3(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("imgData") MultipartFile file) {

        try {
            // 파일 업로드 처리
            String fileNames = minioService.uploadFile(file);
            String url = minioService.getFileUrl(fileNames);

            // 분석 요청 객체 생성
            AnalysisRequest analysisRequest = new AnalysisRequest();
            analysisRequest.setImageUrl(url);

            // WebClient를 사용하여 AI 분석 서비스 호출 (동기 방식)
            AnalysisResponse analysisResponse = webClient.post()
                    .uri(aiServerUrl)
                    .bodyValue(analysisRequest)
                    .retrieve()
                    .bodyToMono(AnalysisResponse.class)
                    .block(Duration.ofMinutes(5)); // 동기 방식으로 호출

            if (analysisResponse == null) {
                throw new RuntimeException("AI 분석 서비스로부터 응답이 없습니다.");
            }

            // 분석 결과 저장
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setImageUrl(analysisRequest.getImageUrl());
            analysisResult.setAnalysis(analysisResponse.getAnalysis());
            analysisResult.setImageName(fileNames);
            aiResultRepository.save(analysisResult);

            // 성공 응답 생성
            UploadResponse response = new UploadResponse(
                    "200",
                    "File uploaded and analyzed successfully",
                    FileUtil.generateFileName(file),
                    analysisResponse.getAnalysis()
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            UploadResponse errorResponse = new UploadResponse("500", e.getMessage(), null, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @Operation(
            summary = "이미지 파일 업로드",
            description = "이미지 파일을 업로드하고 AI 인식 결과를 반환합니다.",
            tags = {"File Upload"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 파일이 업로드되고 분석되었습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content)
    })
    @PostMapping(value = "/upload5", consumes = "multipart/form-data")
    public Mono<ResponseEntity<UploadResponse>> uploadFile5(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("imgData") MultipartFile  file) {

        return Mono.fromCallable(() -> {
                    // 파일 업로드 처리
                    String fileNames = minioService.uploadFile(file);
                    String url = minioService.getFileUrl(fileNames);

                    // 분석 요청 객체 생성
                    AnalysisRequest analysisRequest = new AnalysisRequest();
                    analysisRequest.setImageUrl(url);

                    return Tuples.of(analysisRequest, fileNames); // analysisRequest와 fileNames를 Tuple로 반환
                })
                .flatMap(tuple ->
                        // WebClient를 사용하여 AI 분석 서비스 호출 (논블로킹)
                        webClient.post()
                                .uri(aiServerUrl)
                                .bodyValue(tuple.getT1()) // tuple의 첫 번째 요소 (analysisRequest) 사용
                                .retrieve()
                                .bodyToMono(AnalysisResponse.class)
                                .timeout(Duration.ofMinutes(5))
                                .map(analysisResponse -> Tuples.of(tuple.getT1(), tuple.getT2(), analysisResponse)) // response와 함께 tuple 확장
                )
                .flatMap(tuple -> {
                    // 분석 결과 저장
                    AnalysisResult analysisResult = new AnalysisResult();
                    analysisResult.setImageUrl(tuple.getT1().getImageUrl()); // tuple의 첫 번째 요소 (analysisRequest) 사용
                    analysisResult.setAnalysis(tuple.getT3().getAnalysis()); // tuple의 세 번째 요소 (analysisResponse) 사용
                    analysisResult.setImageName(tuple.getT2()); // tuple의 두 번째 요소 (fileNames) 사용
                    aiResultRepository.save(analysisResult);

                    // 성공 응답 생성
                    UploadResponse response = new UploadResponse(
                            "200",
                            "File uploaded and analyzed successfully",
                            FileUtil.generateFileName(file),
                            tuple.getT3().getAnalysis() // 분석 결과
                    );
                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(e -> {
                    UploadResponse errorResponse = new UploadResponse("500", e.getMessage(), null, null);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }


    @Operation(
            summary = "이미지 파일 업로드",
            description = "이미지 파일을 업로드하고 AI 인식 결과를 반환합니다.",
            tags = {"File Upload"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 파일이 업로드되고 분석되었습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content)
    })
    @PostMapping(value = "/upload-then-response-with-voice", consumes = "multipart/form-data")
    public Mono<ResponseEntity<UploadAndResponseWithVoiceDTO>> uploadFile2(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("imgData") MultipartFile  file) {

        return Mono.fromCallable(() -> {
                    // 파일 업로드 처리
                    String fileNames = minioService.uploadFile(file);
                    String url = minioService.getFileUrl(fileNames);

                    // 분석 요청 객체 생성
                    AnalysisRequest analysisRequest = new AnalysisRequest();
                    analysisRequest.setImageUrl(url);

                    return Tuples.of(analysisRequest, fileNames); // analysisRequest와 fileNames를 Tuple로 반환
                })
                .flatMap(tuple ->
                        // WebClient를 사용하여 AI 분석 서비스 호출 (논블로킹)
                        webClient.post()
                                .uri("http://172.16.17.203:8000/analyze_with_audio")
                                .bodyValue(tuple.getT1()) // tuple의 첫 번째 요소 (analysisRequest) 사용
                                .retrieve()
                                .bodyToMono(AnalysisResponseWithVoice.class)
                                .map(analysisResponse -> Tuples.of(tuple.getT1(), tuple.getT2(), analysisResponse)) // response와 함께 tuple 확장
                )
                .flatMap(tuple -> {
                    System.out.println(tuple.getT3());
                    // 분석 결과 저장
//                    AnalysisResult analysisResult = new AnalysisResult();
//                    analysisResult.setImageUrl(tuple.getT1().getImageUrl()); // tuple의 첫 번째 요소 (analysisRequest) 사용
//                    analysisResult.setAnalysis(tuple.getT3().getAnalysis()); // tuple의 세 번째 요소 (analysisResponse) 사용
//                    analysisResult.setTtsFilename(tuple.getT3().getFileName()); // tuple의 세 번째 요소 (analysisResponse) 사용
//                    analysisResult.setImageName(tuple.getT2()); // tuple의 두 번째 요소 (fileNames) 사용
//                    aiResultRepository.save(analysisResult);
                    String ttsFileUrl;
                    try {
                        ttsFileUrl=  minioService.getFileUrl(tuple.getT3().getFileName());
                    } catch (Exception e) {
                        UploadAndResponseWithVoiceDTO errorResponse = new UploadAndResponseWithVoiceDTO("500", e.getMessage(), null, null,null);
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                    }
                    // 성공 응답 생성
                    UploadAndResponseWithVoiceDTO response = new UploadAndResponseWithVoiceDTO(
                            "200",
                            "File uploaded and analyzed successfully",
                            FileUtil.generateFileName(file),
                            tuple.getT3().getAnalysis(), // 분석 결과,
                            ttsFileUrl
                    );
                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(e -> {
                    UploadAndResponseWithVoiceDTO errorResponse = new UploadAndResponseWithVoiceDTO("300", e.getMessage(), null, null,null);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }
    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 업로드하고 AI 인식 결과를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content)
    })

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

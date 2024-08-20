package com.outsider.midnight.file.command.application.service;

import com.outsider.midnight.file.command.application.dto.AnalysisRequest;
import com.outsider.midnight.file.command.application.dto.AnalysisResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@EnableAsync
public class AIService {

    @Autowired
    private RestTemplate restTemplate;

    @Async
    public CompletableFuture<AnalysisResponse> analyzeImage(AnalysisRequest analysisRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<AnalysisRequest> request = new HttpEntity<>(analysisRequest, headers);
        ResponseEntity<AnalysisResponse> response = restTemplate.exchange(
                "http://172.16.17.203:8000/analysis", // AI 분석 API 엔드포인트
                HttpMethod.POST,
                request,
                AnalysisResponse.class
        );

        return CompletableFuture.completedFuture(response.getBody());
    }
}

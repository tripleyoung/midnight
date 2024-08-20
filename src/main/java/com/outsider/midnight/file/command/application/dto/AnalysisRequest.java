package com.outsider.midnight.file.command.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnalysisRequest {
    @JsonProperty("image_url")
    private String imageUrl;
}

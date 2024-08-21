package com.outsider.midnight.file.command.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnalysisResponseWithVoice {
    private String analysis;
    @JsonProperty("file_name")
    private String fileName;
}

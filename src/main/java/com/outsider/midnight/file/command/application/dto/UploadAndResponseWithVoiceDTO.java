package com.outsider.midnight.file.command.application.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "AI_RES_01 : 이미지 추론 응답",description = "이미지 업로드에 대한 응답")
public class UploadAndResponseWithVoiceDTO {

    @Schema(description = "리턴 코드", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnCode;  // 리턴 코드

    @Schema(description = "리턴 메시지", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnMsg;   // 리턴 메시지

    @Schema(description = "이미지 제목", example = "uploaded_image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imgTitle;    // 이미지 제목

    @Schema(description = "AI 인식 내용 (성공 시에만 전달)", example = "Recognized text from image", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String recogMsg;    // 인식 내용 (선택)
     // 인식 내용 (선택)
    @Schema(description = "TTS URL", example = "https://example.com/item.wav", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String ttsUrl;    // 인식 내용 (선택)
    // Constructor
    public UploadAndResponseWithVoiceDTO(String returnCode, String returnMsg, String imgTitle, String recogMsg,  String ttsUrl) {
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
        this.imgTitle = imgTitle;
        this.recogMsg = recogMsg;
        this.ttsUrl = ttsUrl;
    }


}


package com.outsider.midnight.file.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(title = "AI_RES_01 : 이미지 추론 응답",description = "이미지 업로드에 대한 응답")
public class UploadResponse {

    @Schema(description = "리턴 코드", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnCode;  // 리턴 코드

    @Schema(description = "리턴 메시지", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnMsg;   // 리턴 메시지

    @Schema(description = "이미지 제목", example = "uploaded_image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imgTitle;    // 이미지 제목

    @Schema(description = "AI 인식 내용 (성공 시에만 전달)", example = "Recognized text from image", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String recogMsg;    // 인식 내용 (선택)

    // Constructor
    public UploadResponse(String returnCode, String returnMsg, String imgTitle, String recogMsg) {
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
        this.imgTitle = imgTitle;
        this.recogMsg = recogMsg;
    }

    // Getters and setters
    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }

    public String getReturnMsg() { return returnMsg; }
    public void setReturnMsg(String returnMsg) { this.returnMsg = returnMsg; }

    public String getImgTitle() { return imgTitle; }
    public void setImgTitle(String imgTitle) { this.imgTitle = imgTitle; }

    public String getRecogMsg() { return recogMsg; }
    public void setRecogMsg(String recogMsg) { this.recogMsg = recogMsg; }


}

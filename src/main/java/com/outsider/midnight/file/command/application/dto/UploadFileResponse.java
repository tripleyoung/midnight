package com.outsider.midnight.file.command.application.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "FILE_RES_02 : 파일 업로드 응답",description = "파일 업로드에 대한 응답")
public class UploadFileResponse {

    @Schema(description = "리턴 코드", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnCode;  // 리턴 코드

    @Schema(description = "리턴 메시지", example = "SUCCESS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnMsg;   // 리턴 메시지

    @Schema(description = "파일 제목", example = "uploaded_image.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;    // 이미지 제목

 // 인식 내용 (선택)

    // Constructor
    public UploadFileResponse(String returnCode, String returnMsg, String fileName) {
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
        this.fileName = fileName;
    }





}

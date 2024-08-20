package com.outsider.midnight.file.command.application.dto;

public class UploadResponse {
    private String returnCode;
    private String returnMsg;
    private String imgTitle;
    private String recogMsg;
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

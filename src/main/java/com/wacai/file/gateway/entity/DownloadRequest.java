package com.wacai.file.gateway.entity;

/**
 * Created by fulushou on 2017/11/3.
 */
public class DownloadRequest {

    private String fileName;

    private String token;

    private String appKey;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    @Override
    public String toString() {
        return "DownloadRequestEntity{" +
                ", fileName='" + fileName + '\'' +
                ", token='" + token + '\'' +
                ", appKey='" + appKey + '\'' +
                '}';
    }
}

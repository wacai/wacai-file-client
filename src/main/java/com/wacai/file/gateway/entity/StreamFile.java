package com.wacai.file.gateway.entity;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by fulushou on 2018/5/10.
 */
public class StreamFile implements Serializable {

    InputStream inputStream;
    //指定文件名，可空
    String filename;
    //文件有效时间,单位s;达到预定时间,自动删除
    Long expireSeconds;

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}

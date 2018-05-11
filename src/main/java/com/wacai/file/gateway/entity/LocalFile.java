package com.wacai.file.gateway.entity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by fulushou on 2017/11/6.
 */
public class LocalFile implements Serializable{

    private File file;

    //指定文件名
    private String filename;

    //文件有效时间,单位s;达到预定时间,自动删除
    Long expireSeconds;

    public File getFile() {
        return file;
    }

    public void setFile(File file) throws IOException {
        this.file = file;
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

    @Override
    public String toString() {
        return "LocalFile{" +
                "file=" + file +
                ", filename='" + filename + '\'' +
                ", expireSeconds=" + expireSeconds +
                '}';
    }
}

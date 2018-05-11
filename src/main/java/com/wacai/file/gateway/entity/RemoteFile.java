package com.wacai.file.gateway.entity;

import java.io.Serializable;

/**
 * Created by fulushou on 2017/11/6.
 */
public class RemoteFile implements Serializable{

    public RemoteFile(String filename, String namespace, String secretKey) {
        this.filename = filename;
        this.namespace = namespace;
        this.secretKey = secretKey;
    }

    public RemoteFile(String filename, String namespace) {
        this.filename = filename;
        this.namespace = namespace;
    }


    public RemoteFile() {
    }

    //生成的新文件名
    private String filename;

    //原始文件名
    private String originalName;

    private String namespace;

    private String secretKey;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    @Override
    public String toString() {
        return "RemoteFile{" +
                "filename='" + filename + '\'' +
                ", originalName='" + originalName + '\'' +
                ", namespace='" + namespace + '\'' +
                ", secretKey='" + secretKey + '\'' +
                '}';
    }
}

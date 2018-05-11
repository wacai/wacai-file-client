package com.wacai.file.gateway.entity;

/**
 * Created by fulushou on 2017/12/11.
 */
public class Response<T> {

    public static final int SUCCESS = 0;

    public int code;

    public String error;

    T data;

    public boolean isSuccess(){
        return code == SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}

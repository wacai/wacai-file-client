package com.wacai.file.token.response;

import lombok.Data;

@Data
public class WacaiErrorResponse {

  private int code;

  private String error;
}

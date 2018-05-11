package com.wacai.file.token.response;


import lombok.Data;

import java.io.Serializable;

@Data
public class AccessToken implements Serializable {

  private String accessToken;

  private String tokenType;

  private Integer expires;

  private String refreshToken;
}


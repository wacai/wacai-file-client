package com.wacai.file.token;

import com.wacai.file.token.response.AccessToken;
import com.wacai.file.token.util.SignUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by fulushou on 2018/5/10.
 */
public class ApplyToken {
    RestTemplate restTemplate = new RestTemplate();

    private String appKey;
    private String appSecret;
    private String gatewayAuthUrl;

    public ApplyToken(String appKey, String appSecret, String gatewayAuthUrl) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.gatewayAuthUrl = gatewayAuthUrl;
    }

    public AccessToken refreshAccessToken(String refreshToken) {
        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(appKey + "refresh_token" + refreshToken + timestamp,
                appSecret);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("app_key", appKey);
        body.add("grant_type", "refresh_token");
        body.add("timestamp", String.valueOf(timestamp));
        body.add("refresh_token", refreshToken);
        body.add("sign", sign);
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(body);
        ResponseEntity<Map> mapResponseEntity = restTemplate.postForEntity(gatewayAuthUrl + "/refresh",request,Map.class);
        return tokenTransfer(mapResponseEntity.getBody());
    }


    public AccessToken applyAccessToken() {
        long timestamp = System.currentTimeMillis();
        String sign = SignUtil.generateSign(appKey + "client_credentials" + timestamp, appSecret);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();

        body.add("app_key", appKey);
        body.add("grant_type", "client_credentials");
        body.add("timestamp", String.valueOf(timestamp));
        body.add("sign", sign);
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(body);
        ResponseEntity<Map> mapResponseEntity = restTemplate.postForEntity(gatewayAuthUrl + "/token",request,Map.class);
        mapResponseEntity.getStatusCode();
        return tokenTransfer(mapResponseEntity.getBody());
    }

    private AccessToken tokenTransfer(Map mapParam) {
        AccessToken token = new AccessToken();
        token.setAccessToken(String.valueOf(mapParam.get("access_token")));
        token.setTokenType(String.valueOf(mapParam.get("token_type")));
        token.setExpires(Integer.valueOf(String.valueOf(mapParam.get("expires_in"))));
        token.setRefreshToken(String.valueOf(mapParam.get("refresh_token")));
        return token;
    }
}

/*
package com.wacai.file.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wacai.file.gateway.entity.LocalFile;
import com.wacai.file.gateway.entity.RemoteFile;
import com.wacai.file.gateway.entity.Response;
import com.wacai.file.gateway.entity.StreamFile;
import com.wacai.file.token.ApplyToken;
import com.wacai.file.token.response.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

*/
/**
 * Created by fulushou on 2018/5/10.
 *//*

@Slf4j
public class FileManager {
    private String appKey = "3y3nmtkx3ykc";
    private String appSecret = "8cnukuk9tu7annnr";
    private String gatewayAuthUrl = "http://open-token-boot.loan.k2.test.wacai.info/token/auth";
    private volatile String xAccessToken = null;
    private ApplyToken applyToken;
    private RestTemplate client = new RestTemplate();
    private String url;
    private String namespace;
    private int execTimes = 3;

    public FileManager(String url, String namespace,String appKey,String appSecret,String gatewayAuthUrl) {
        this.url = url;
        this.namespace = namespace;
        this.applyToken = new ApplyToken(appKey,appSecret,gatewayAuthUrl);
        this.setTimeout(10000L);
    }

    public Response<List<RemoteFile>> uploadFilesRetry(List<LocalFile> localFiles) throws IOException {
        for (int i = 0; i < execTimes; i++) {
            try{
                return uploadFiles(localFiles);
            }catch (Exception e){
                xAccessToken = null;
                log.error("upload files:{} exception:",localFiles,e);
            }
        }
        Response response = new Response();
        response.code = 1;
        response.error = "upload error try three times";
        return response;
    }

    public Response<List<RemoteFile>> uploadStreamsRetry(List<StreamFile> streamFiles) throws IOException {
        for (int i = 0; i < execTimes; i++) {
            try{
                return uploadStreams(streamFiles);
            }catch (Exception e){
                xAccessToken = null;
                log.error("upload streams:{} exception:",streamFiles,e);
            }
        }
        Response response = new Response();
        response.code = 1;
        response.error = "upload streams try three times";
        return response;
    }

    public Response<RemoteFile> uploadFileRetry(LocalFile localFile) throws IOException {
        for (int i = 0; i < execTimes; i++) {
            try{
                return uploadFile(localFile);
            }catch (Exception e){
                xAccessToken = null;
                log.error("upload file:{} exception:",localFile,e);
            }
        }
        Response response = new Response();
        response.code = 1;
        response.error = "upload error try three times";
        return response;
    }

    public Response<RemoteFile> uploadStreamRetry(StreamFile streamFile) throws IOException {
        for (int i = 0; i < execTimes; i++) {
            try{
                return uploadStream(streamFile);
            }catch (Exception e){
                xAccessToken = null;
                log.error("upload stream:{} exception:",streamFile,e);
            }
        }
        Response response = new Response();
        response.code = 1;
        response.error = "upload stream try three times";
        return response;
    }



    private Response<List<RemoteFile>> uploadFiles(List<LocalFile> localFiles) throws IOException {
        MultiValueMap<String, Object> body = this.addLocalFiles(localFiles);
        HttpHeaders headers = this.generateHeaders();
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
        String tempUrl =  url.endsWith("/") ? (url + "upload/normal/" + namespace)  : (url + "/upload/normal/" + namespace);
        final ResponseEntity<String> result = client.postForEntity(tempUrl,request,String.class);
        ObjectMapper mapper = new ObjectMapper();
        Response<List<RemoteFile>> res = mapper.readValue(result.getBody(),new TypeReference<Response<List<RemoteFile>>>(){});
        return res;
    }

    private Response<List<RemoteFile>> uploadStreams(List<StreamFile> streamFiles) throws IOException {
        MultiValueMap<String, Object> body = this.addStreamFiles(streamFiles);
        HttpHeaders headers = this.generateHeaders();
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
        String tempUrl =  url.endsWith("/") ? (url + "upload/normal/" + namespace)  : (url + "/upload/normal/" + namespace);
        final ResponseEntity<String> result = client.postForEntity(tempUrl,request,String.class);
        ObjectMapper mapper = new ObjectMapper();
        Response<List<RemoteFile>> res = mapper.readValue(result.getBody(),new TypeReference<Response<List<RemoteFile>>>(){});
        return res;
    }

    private Response<RemoteFile> uploadFile(LocalFile localFile) throws IOException {
        MultiValueMap<String, Object> body = this.addLocalFile(localFile);
        HttpHeaders headers = this.generateHeaders();
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
        String tempUrl =  url.endsWith("/") ? (url + "upload/online/" + namespace)  : (url + "/upload/online/" + namespace);
        final ResponseEntity<String> result = client.postForEntity(tempUrl,request,String.class);
        ObjectMapper mapper = new ObjectMapper();
        Response<RemoteFile> res = mapper.readValue(result.getBody(),new TypeReference<Response<RemoteFile>>(){});
        return res;
    }

    public Response<RemoteFile> uploadStream(StreamFile streamFile) throws IOException {
        MultiValueMap<String, Object> body = this.addStreamFile(streamFile);
        HttpHeaders headers = this.generateHeaders();
        final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
        String tempUrl =  url.endsWith("/") ? (url + "upload/online/" + namespace)  : (url + "/upload/online/" + namespace);
        final ResponseEntity<String> result = client.postForEntity(tempUrl,request,String.class);
        ObjectMapper mapper = new ObjectMapper();
        Response<RemoteFile> res = mapper.readValue(result.getBody(),new TypeReference<Response<RemoteFile>>(){});
        return res;
    }

    private LinkedMultiValueMap<String, Object> addStreamFile(final StreamFile streamFile){
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("file",new InputStreamResource(streamFile.getInputStream()));
        body.add("filename",streamFile.getFilename());
        body.add("expireSeconds",streamFile.getExpireSeconds());
        return body;
    }

    private LinkedMultiValueMap<String, Object> addLocalFile(final LocalFile localFile){
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("file",new FileSystemResource(localFile.getFile()));
        body.add("filename",localFile.getFilename());
        body.add("expireSeconds",localFile.getExpireSeconds());
        return body;
    }

    private LinkedMultiValueMap<String, Object> addStreamFiles(final List<StreamFile> streamFiles){
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        for (StreamFile streamFile : streamFiles) {
            final String filename = streamFile.getFilename();
            body.add("files",new InputStreamResource(streamFile.getInputStream()){
                @Override
                public String getFilename() {
                    return filename == null?super.getFilename():filename;
                }
            });
        }
        return body;
    }

    private LinkedMultiValueMap<String, Object>  addLocalFiles(final List<LocalFile> localFiles){
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        for (LocalFile localFile : localFiles) {
            final String filename = localFile.getFilename();
            body.add("files",new FileSystemResource(localFile.getFile()){
                @Override
                public String getFilename() {
                    return filename == null?super.getFilename():filename;
                }
            });
        }
        return body;
    }

    public byte[]  downloadRetry(RemoteFile remoteFile) {
        for (int i = 0; i < execTimes; i++) {
            try{
                return download(remoteFile);
            }catch (Exception e){
                xAccessToken = null;
                log.error("upload remoteFile:{} exception:",remoteFile,e);
            }
        }
        return null;
    }

    public byte[]  downloadSecretKeyRetry(RemoteFile remoteFile) {
        for (int i = 0; i < execTimes; i++) {
            try{
                return downloadSecretKey(remoteFile);
            }catch (Exception e){
                xAccessToken = null;
                log.error("upload downloadSecretKey:{} exception:",remoteFile,e);
            }
        }
        return null;
    }
    
    private byte[] download(RemoteFile remoteFile) {
        HttpHeaders headers = this.generateHeaders();
        final HttpEntity<String> requestEntity = new HttpEntity<String>(null, headers);
        String tempUrl =  url.endsWith("/") ? (url +  "download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/")  : (url +  "/download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/");
        ResponseEntity<byte[]> result = client.exchange(tempUrl, HttpMethod.GET, requestEntity, byte[].class);
        return result.getBody();
    }

    private byte[] downloadSecretKey(RemoteFile remoteFile) {
        if(remoteFile.getSecretKey() == null ) {
            throw new RuntimeException("secretKey can not be null");
        }
        String tempUrl =  url.endsWith("/") ? (url +  "download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/" + remoteFile.getSecretKey())  : (url +  "/download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/" + remoteFile.getSecretKey());
        ResponseEntity<byte[]> result = client.exchange(tempUrl, HttpMethod.GET,null, byte[].class);
        return result.getBody();
    }

    private HttpHeaders generateHeaders() {

        if(xAccessToken == null){
            synchronized (lock) {
                if(xAccessToken == null) {
                    AccessToken accessToken = applyToken.applyAccessToken();
                    xAccessToken = accessToken.getAccessToken();
                }
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("X-access-token",xAccessToken);
        headers.add("appKey",appKey);
        return headers;
    }

    public void setTimeout(Long timeout) {
        if (timeout != null && client.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory) client.getRequestFactory()).setReadTimeout(timeout.intValue());
        }
    }

    public void setExecTimes(int execTimes) {
        if(execTimes >=1 && execTimes <= 10)
        this.execTimes = execTimes;
    }

    private Object lock = new Object();
}
*/

package com.wacai.file.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wacai.file.gateway.entity.LocalFile;
import com.wacai.file.gateway.entity.RemoteFile;
import com.wacai.file.gateway.entity.Response;
import com.wacai.file.gateway.entity.StreamFile;
import com.wacai.file.http.HttpClientFactory;
import com.wacai.file.token.ApplyToken;
import com.wacai.file.token.response.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by fulushou on 2018/5/10.
 */
@Slf4j
public class CapableFileManager {
    private String appKey = "3y3nmtkx3ykc";
    private String appSecret = "8cnukuk9tu7annnr";
    private String gatewayAuthUrl = "http://open-token-boot.loan.k2.test.wacai.info/token/auth";
    private volatile String xAccessToken = null;
    private ApplyToken applyToken;
    private String url;
    private String namespace;
    private int execTimes = 3;

    //可以自己进行实现，如果不自己实现，则使用默认值
    CloseableHttpClient client = null;
    //可以自己进行设置参数，替换默认值
    private HttpClientFactory httpClientFactory = new HttpClientFactory();

    public CapableFileManager(String url, String namespace, String appKey, String appSecret, String gatewayAuthUrl) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.gatewayAuthUrl = gatewayAuthUrl;
        this.url = url;
        this.namespace = namespace;
        this.applyToken = new ApplyToken(appKey,appSecret,gatewayAuthUrl);
        this.setTimeout(10000);
        client = httpClientFactory.getHttpClient();
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
        response.error = "upload error execTimes:" + execTimes;
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
        String tempUrl =  url.endsWith("/") ? (url + "upload/normal/" + namespace)  : (url + "/upload/normal/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        this.generateHeaders(httpPost);
        httpPost.setEntity(assemblyFilesEntity(localFiles));

        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<List<RemoteFile>> res = mapper.readValue(result,new TypeReference<Response<List<RemoteFile>>>(){});
        return res;
    }

    private Response<List<RemoteFile>> uploadStreams(List<StreamFile> streamFiles) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/normal/" + namespace)  : (url + "/upload/normal/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        this.generateHeaders(httpPost);
        httpPost.setEntity(assemblyStreamsEntity(streamFiles));

        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<List<RemoteFile>> res = mapper.readValue(result,new TypeReference<Response<List<RemoteFile>>>(){});
        return res;
    }

    public Response<RemoteFile> uploadFile(LocalFile localFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/online/" + namespace)  : (url + "/upload/online/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        this.generateHeaders(httpPost);
        httpPost.setEntity(assemblyFileEntity(localFile));

        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<RemoteFile> res = mapper.readValue(result,new TypeReference<Response<RemoteFile>>(){});
        return res;
    }

    private Response<RemoteFile> uploadStream(StreamFile streamFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/online/" + namespace)  : (url + "/upload/online/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        this.generateHeaders(httpPost);
        httpPost.setEntity(assemblyStreamEntity(streamFile));

        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<RemoteFile> res = mapper.readValue(result,new TypeReference<Response<RemoteFile>>(){});
        return res;
    }

    public HttpEntity assemblyFileEntity(final LocalFile localFile){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("file", localFile.getFile());
        builder.addTextBody("filename",localFile.getFilename());
        if(localFile.getExpireSeconds() != null)
        builder.addTextBody("expireSeconds",localFile.getExpireSeconds().toString());
        HttpEntity entity = builder.build();
        return entity;
    }

    public HttpEntity assemblyStreamEntity(final StreamFile streamFile) throws FileNotFoundException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        builder.addBinaryBody("file", new FileInputStream("D:\\1.txt"));
//        builder.addTextBody("filename",streamFile.getFilename());
        ContentBody contentBody = new InputStreamBody(streamFile.getInputStream(),streamFile.getFilename());
        builder.addPart("file", contentBody);
        if(streamFile.getExpireSeconds() != null)
        builder.addTextBody("expireSeconds",streamFile.getExpireSeconds().toString());
        HttpEntity entity = builder.build();
        return entity;
    }

    public HttpEntity assemblyFilesEntity(final List<LocalFile> localFiles){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (LocalFile streamFile : localFiles) {
            builder.addBinaryBody("files", streamFile.getFile());
        }
        HttpEntity entity = builder.build();
        return entity;
    }

    public HttpEntity assemblyStreamsEntity(final List<StreamFile> streamFiles){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (StreamFile streamFile : streamFiles) {//todo 好奇怪;明明是封装啊
            ContentBody contentBody = new InputStreamBody(streamFile.getInputStream(),streamFile.getFilename());
            builder.addPart("files", contentBody);
        }
        HttpEntity entity = builder.build();
        return entity;
    }

    public InputStream  downloadRetry(RemoteFile remoteFile) {
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

    public InputStream  downloadSecretKeyRetry(RemoteFile remoteFile) {
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
    
    private InputStream download(RemoteFile remoteFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url +  "download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/")  : (url +  "/download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/");
        HttpGet httpGet = new HttpGet(tempUrl);
        this.generateHeaders(httpGet);
        CloseableHttpResponse response = client.execute(httpGet);
        if(response.getStatusLine().getStatusCode() != 200){
            log.error("download exception:{}", EntityUtils.toString(response.getEntity()));
            return null;
        }
        return response.getEntity().getContent();
    }

    private InputStream downloadSecretKey(RemoteFile remoteFile) throws IOException {
        if(remoteFile.getSecretKey() == null ) {
            throw new RuntimeException("secretKey can not be null");
        }
        String tempUrl =  url.endsWith("/") ? (url +  "download/online/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/" + remoteFile.getSecretKey())  : (url +  "/download/online/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/" + remoteFile.getSecretKey());
        HttpGet httpGet = new HttpGet(tempUrl);
        CloseableHttpResponse response = client.execute(httpGet);
        if(response.getStatusLine().getStatusCode() != 200){
            log.error("downloadSecretKey exception:{}", EntityUtils.toString(response.getEntity()));
            return null;
        }
        return response.getEntity().getContent();
    }

    private void generateHeaders(HttpMessage httpMessage) {

        if(xAccessToken == null){
            synchronized (lock) {
                if(xAccessToken == null) {
                    AccessToken accessToken = applyToken.applyAccessToken();
                    xAccessToken = accessToken.getAccessToken();
                }
            }
        }
        httpMessage.setHeader("X-access-token",xAccessToken);
        httpMessage.setHeader("appKey",appKey);
    }

    public void setTimeout(int timeout) {
        httpClientFactory.setConnectTimeout(timeout);
        httpClientFactory.setSocketTimeout(timeout);
        httpClientFactory.setConnectionRequestTimeout(timeout);
    }

    public void setExecTimes(int execTimes) {
        if(execTimes >=1 && execTimes <= 10)
        this.execTimes = execTimes;
    }

    private Object lock = new Object();
}

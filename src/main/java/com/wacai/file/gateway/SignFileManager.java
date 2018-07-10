package com.wacai.file.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wacai.file.gateway.entity.LocalFile;
import com.wacai.file.gateway.entity.RemoteFile;
import com.wacai.file.gateway.entity.Response;
import com.wacai.file.gateway.entity.StreamFile;
import com.wacai.file.http.HttpClientFactory;
import com.wacai.file.token.ApplyToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by fulushou on 2018/7/9.
 */
@Slf4j
public class SignFileManager {
    private String appKey = "";
    private String appSecret = "";
    private String gatewayAuthUrl = "";
    private volatile String xAccessToken = null;
    private ApplyToken applyToken;
    private String url;
    private String namespace;

    //可以自己进行实现，如果不自己实现，则使用默认值
    CloseableHttpClient client = null;
    //可以自己进行设置参数，替换默认值
    private HttpClientFactory httpClientFactory = new HttpClientFactory();

    public SignFileManager(String url, String namespace, String appKey, String appSecret, String gatewayAuthUrl) {
        this.url = url;
        this.namespace = namespace;
        this.applyToken = new ApplyToken(appKey,appSecret,gatewayAuthUrl);
        this.setTimeout(10000);
        client = httpClientFactory.getHttpClient();
    }


    public Response<List<RemoteFile>> uploadFiles(List<LocalFile> localFiles) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/normal/" + namespace)  : (url + "/upload/normal/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        httpPost.setHeader("appKey",appKey);
        StringBuffer plainText = new StringBuffer("appKey" + "=" + appKey);
        httpPost.setEntity(assemblyFilesEntity(localFiles,plainText));
        httpPost.setHeader("sign",generateSign(plainText.toString(),appSecret));
        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<List<RemoteFile>> res = mapper.readValue(result,new TypeReference<Response<List<RemoteFile>>>(){});
        return res;
    }

    public Response<List<RemoteFile>> uploadStreams(List<StreamFile> streamFiles) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/normal/" + namespace)  : (url + "/upload/normal/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        httpPost.setHeader("appKey",appKey);
        StringBuffer plainText = new StringBuffer("appKey" + "=" + appKey) ;
        httpPost.setEntity(assemblyStreamsEntity(streamFiles,plainText));
        httpPost.setHeader("sign",generateSign(plainText.toString(),appSecret));

        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<List<RemoteFile>> res = mapper.readValue(result,new TypeReference<Response<List<RemoteFile>>>(){});
        return res;
    }

    public Response<RemoteFile> uploadFile(LocalFile localFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/online/" + namespace)  : (url + "/upload/online/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        httpPost.setHeader("appKey",appKey);
        StringBuffer plainText = new StringBuffer("appKey" + "=" + appKey) ;
        httpPost.setEntity(assemblyFileEntity(localFile,plainText));
        httpPost.setHeader("sign",generateSign(plainText.toString(),appSecret));
        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<RemoteFile> res = mapper.readValue(result,new TypeReference<Response<RemoteFile>>(){});
        return res;
    }

    public Response<RemoteFile> uploadStream(StreamFile streamFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/online/" + namespace)  : (url + "/upload/online/" + namespace);
        HttpPost httpPost = new HttpPost(tempUrl);
        httpPost.setHeader("appKey",appKey);
        StringBuffer plainText = new StringBuffer("appKey" + "=" + appKey);
        httpPost.setEntity(assemblyStreamEntity(streamFile,plainText));
        httpPost.setHeader("sign",generateSign(plainText.toString(),appSecret));
        String result = client.execute(httpPost, new BasicResponseHandler());
        ObjectMapper mapper = new ObjectMapper();
        Response<RemoteFile> res = mapper.readValue(result,new TypeReference<Response<RemoteFile>>(){});
        return res;
    }

    private HttpEntity assemblyFileEntity(final LocalFile localFile,StringBuffer plainText){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("file", localFile.getFile());
        plainText.append("|").append(localFile.getFilename());
        builder.addTextBody("filename",localFile.getFilename());
        if(localFile.getExpireSeconds() != null)
            builder.addTextBody("expireSeconds",localFile.getExpireSeconds().toString());
        HttpEntity entity = builder.build();
        return entity;
    }

    private HttpEntity assemblyStreamEntity(final StreamFile streamFile,StringBuffer plainText) throws FileNotFoundException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody contentBody = new InputStreamBody(streamFile.getInputStream(),streamFile.getFilename());
        builder.addPart("file", contentBody);
        plainText.append("|").append(contentBody.getFilename());
        if(streamFile.getExpireSeconds() != null)
            builder.addTextBody("expireSeconds",streamFile.getExpireSeconds().toString());
        HttpEntity entity = builder.build();
        return entity;
    }

    private HttpEntity assemblyFilesEntity(final List<LocalFile> localFiles,StringBuffer plainText){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (LocalFile localFile : localFiles) {
            builder.addBinaryBody("files", localFile.getFile());
            plainText.append("|").append(localFile.getFilename());
        }
        HttpEntity entity = builder.build();
        return entity;
    }

    private HttpEntity assemblyStreamsEntity(final List<StreamFile> streamFiles,StringBuffer plainText){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (StreamFile streamFile : streamFiles) {//todo 好奇怪;明明是封装啊
            ContentBody contentBody = new InputStreamBody(streamFile.getInputStream(),streamFile.getFilename());
            builder.addPart("files", contentBody);
            plainText.append("|").append(contentBody.getFilename());
        }
        HttpEntity entity = builder.build();
        return entity;
    }

    public InputStream download(RemoteFile remoteFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url +  "download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/")  : (url +  "/download/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/");
        HttpGet httpGet = new HttpGet(tempUrl);
        httpGet.setHeader("appKey",appKey);
        String plainText = "appKey" + "=" + appKey + "|" + remoteFile.getFilename();
        httpGet.setHeader("sign",generateSign(plainText,appSecret));
        CloseableHttpResponse response = client.execute(httpGet);
        if(response.getStatusLine().getStatusCode() != 200){
            ObjectMapper mapper = new ObjectMapper();
            Response response1 = mapper.readValue(response.getEntity().getContent(),Response.class);
            log.error("download exception:{}", response1);
            return null;
        }
        return response.getEntity().getContent();
    }

    private void generateHeaders(HttpMessage httpMessage) {
        httpMessage.setHeader("appKey",appKey);
        httpMessage.setHeader("sign","sing");
    }


    private static String generateSign(String plainText, String appSecret) {
        Mac mac;
        String algorithm = "hmacSha256";
        try {
            mac = Mac.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm, e);
        }
        try {
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), algorithm));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("invalid key appSecret : " + appSecret, e);
        }
        byte[] signatureBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64URLSafeString(signatureBytes);
    }


    public void setTimeout(int timeout) {
        httpClientFactory.setConnectTimeout(timeout);
        httpClientFactory.setSocketTimeout(timeout);
        httpClientFactory.setConnectionRequestTimeout(timeout);
    }
}

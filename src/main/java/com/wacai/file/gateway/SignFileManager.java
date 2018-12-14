package com.wacai.file.gateway;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wacai.file.gateway.entity.LocalFile;
import com.wacai.file.gateway.entity.RemoteFile;
import com.wacai.file.gateway.entity.Response;
import com.wacai.file.gateway.entity.StreamFile;
import com.wacai.file.http.HttpClientFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by fulushou on 2018/7/9.
 */
@Slf4j
public class SignFileManager {
	
	public static final ContentType TEXT_PLAIN_UTF8 = ContentType.create("text/plain", Consts.UTF_8);
	
    private String appKey = "";
    private String appSecret = "";
    private String url;
    private String namespace;

    //可以自己进行实现，如果不自己实现，则使用默认值
    CloseableHttpClient client = null;
    //可以自己进行设置参数，替换默认值
    private HttpClientFactory httpClientFactory = new HttpClientFactory();

    public SignFileManager(String url, String namespace, String appKey, String appSecret) {
        this(url, namespace, appKey, appSecret, 10000);
    }
    
    public SignFileManager(String url, String namespace, String appKey, String appSecret, int timeoutInMS) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.url = url;
        this.namespace = namespace;
        this.setTimeout(timeoutInMS);
        client = httpClientFactory.getHttpClient();
    }

    public Response<List<RemoteFile>> uploadFiles(List<LocalFile> localFiles) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url + "upload/sign/normal/" + namespace)  : (url + "/upload/sign/normal/" + namespace);
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
        String tempUrl =  url.endsWith("/") ? (url + "upload/sign/normal/" + namespace)  : (url + "/upload/sign/normal/" + namespace);
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
        String tempUrl =  url.endsWith("/") ? (url + "upload/sign/online/" + namespace)  : (url + "/upload/sign/online/" + namespace);
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
        String tempUrl =  url.endsWith("/") ? (url + "upload/sign/online/" + namespace)  : (url + "/upload/sign/online/" + namespace);
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
        if (localFile.getFilename() == null || "".equals(localFile.getFilename().trim())) {
        	 plainText.append("|").append(localFile.getFile().getName());
        } else {
        	 plainText.append("|").append(localFile.getFilename());
             builder.addTextBody("filename",localFile.getFilename(), TEXT_PLAIN_UTF8);
        }
        if(localFile.getExpireSeconds() != null)
            builder.addTextBody("expireSeconds",localFile.getExpireSeconds().toString());
        HttpEntity entity = builder.build();
        return entity;
    }

    private HttpEntity assemblyStreamEntity(final StreamFile streamFile,StringBuffer plainText) throws FileNotFoundException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        String orginalFile = streamFile.getFilename();
        if (orginalFile == null || "".equals(orginalFile.trim())) {
        	orginalFile = UUID.randomUUID().toString();
        }
        ContentBody contentBody = new InputStreamBody(streamFile.getInputStream(), orginalFile);
        builder.addPart("file", contentBody);
        plainText.append("|").append(contentBody.getFilename());
        builder.addTextBody("filename", contentBody.getFilename(), TEXT_PLAIN_UTF8);
        if(streamFile.getExpireSeconds() != null)
            builder.addTextBody("expireSeconds",streamFile.getExpireSeconds().toString());
        HttpEntity entity = builder.build();
        return entity;
    }

    private HttpEntity assemblyFilesEntity(final List<LocalFile> localFiles,StringBuffer plainText){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (LocalFile localFile : localFiles) {
            builder.addPart("files", new FileBody(localFile.getFile(), getContentType(localFile.getFile().getName())));
            plainText.append("|").append(localFile.getFile().getName());
        }
        HttpEntity entity = builder.build();
        return entity;
    }

    private HttpEntity assemblyStreamsEntity(final List<StreamFile> streamFiles,StringBuffer plainText){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (StreamFile streamFile : streamFiles) {//todo 好奇怪;明明是封装啊
            String orginalFilename = streamFile.getFilename();
            if (orginalFilename == null || "".equals(orginalFilename.trim())) {
            	orginalFilename = UUID.randomUUID().toString();
            }
			ContentBody contentBody = new InputStreamBody(streamFile.getInputStream(), orginalFilename);
            builder.addPart("files", contentBody);
            plainText.append("|").append(contentBody.getFilename());
        }
        builder.setCharset(StandardCharsets.UTF_8);
        HttpEntity entity = builder.build();
        return entity;
    }

    public InputStream download(RemoteFile remoteFile) throws IOException {
        String tempUrl =  url.endsWith("/") ? (url +  "download/sign/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/")  : (url +  "/download/sign/" + remoteFile.getNamespace() + "/" + remoteFile.getFilename() + "/");
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

    public static ContentType getContentType(String filename){
        try {
            Path path = Paths.get(filename);
            String contentType = Files.probeContentType(path);
            if(contentType == null){
                return ContentType.APPLICATION_OCTET_STREAM;
            }
            return ContentType.create(contentType);
        }catch (Exception e){
            return ContentType.APPLICATION_OCTET_STREAM;
        }
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

    /**
     * 设置超时时间。
     * 注意： 线程不安全。 因为此方法会重新初始化client，并销毁之前的client。
     * @param timeout 超时时间 单位毫秒。
     */
    public void setTimeout(int timeout) {
        httpClientFactory.setConnectTimeout(timeout);
        httpClientFactory.setSocketTimeout(timeout);
        httpClientFactory.setConnectionRequestTimeout(timeout);
        
        /**
         * 销毁之前的client实例，否则可能会存在资源泄露
         */
        try {
			this.destroy();
		} catch (Throwable e) {
			e.printStackTrace();
		}
        
        /**
         * 设置完之后 需要重建client实例，否则还是之前的参数
         */
        client = httpClientFactory.getHttpClient();
    }
    
    /**
     * 用完该实例后 需要调用destroy方法，销毁相应资源。尤其在多实例使用时。
     * @throws IOException 
     */
    public void destroy() throws IOException {
    	if (this.client == null) {
    		return;
    	}
    	this.client.close();
    }
}

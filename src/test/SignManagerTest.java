import com.wacai.file.gateway.SignFileManager;
import com.wacai.file.gateway.entity.LocalFile;
import com.wacai.file.gateway.entity.RemoteFile;
import com.wacai.file.gateway.entity.Response;
import com.wacai.file.gateway.entity.StreamFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fulushou on 2018/5/10.
 */
public class SignManagerTest {

    static Logger log = LoggerFactory.getLogger(SignManagerTest.class);
//    //配置参数
    static String appKey = "3y3nmtkx3ykc";
    static String appSecret = "8cnukuk9tu7annnr";
    static String gatewayAuthUrl = "http://open-token-boot.loan.k2.test.wacai.info/token/auth";
    static String url = "https://file.ngrok.wacaiyun.com";
    static String namespace = "test";

    public static void main(String args[]) throws IOException {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");// "stdout"为标准输出格式，"debug"为调试模式
        SignFileManager fileManager = new SignFileManager(url,namespace,appKey,appSecret,gatewayAuthUrl);
        fileManager.setTimeout(1000);//超时时间
        uploadOneFile(fileManager); //上传单个文件
        uploadFiles(fileManager); //上传多个文件
        Response<RemoteFile> response = uploadOneStream(fileManager);
        uploadOneStreams(fileManager); //以流的方式上传文件
        download(fileManager,response.getData().getFilename()); //下载文件方式1
    }

    //上传单个文件
    private static void uploadOneFile(SignFileManager fileManager) throws IOException {
        LocalFile localFile = new LocalFile();
        localFile.setFile(new File("D:\\1.txt"));
        localFile.setFilename("file.txt"); //上传到服务器时的名称
        Response<RemoteFile> response = fileManager.uploadFile(localFile);
        log.info("uploadOneFile response:{}",response);
    }

    private static Response<RemoteFile> uploadOneStream(SignFileManager fileManager) throws IOException {
        StreamFile streamFile = new StreamFile();
        streamFile.setInputStream(new FileInputStream("D:\\1.txt"));
        streamFile.setFilename("stream.txt"); //上传到服务器时的名称
        Response<RemoteFile> response = fileManager.uploadStream(streamFile);
        log.info("response:{}",response);
        return response;
    }

    private static void uploadOneStreams(SignFileManager fileManager) throws IOException {
        StreamFile streamFile = new StreamFile();
        streamFile.setInputStream(new FileInputStream("D:\\1.txt"));
        streamFile.setFilename("stream.txt"); //上传到服务器时的名称
        List<StreamFile> streamFiles = new ArrayList<>();
        streamFiles.add(streamFile);
        Response<List<RemoteFile>> response = fileManager.uploadStreams(streamFiles);
        log.info("response:{}",response);
    }

    private static void uploadFiles(SignFileManager fileManager) throws IOException {
        List<LocalFile> localFileList = new ArrayList<>();

        LocalFile localFile = new LocalFile();
        localFile.setFile(new File("D:\\1.txt"));
        localFile.setFilename("1111.txt"); //上传多个文件时,此属性不起作用
        localFileList.add(localFile);

        LocalFile localFile2 = new LocalFile();
        localFile2.setFile(new File("D:\\2.txt"));
        localFile2.setFilename("2222.txt"); //上传多个文件时,此属性不起作用
        localFileList.add(localFile2);

        Response<List<RemoteFile>> response = fileManager.uploadFiles(localFileList);
        log.info("responseList:{}",response);
    }

    private static void download(SignFileManager fileManager,String filename) throws IOException {
        RemoteFile remoteFile = new RemoteFile(filename,namespace);
        InputStream is = fileManager.download(remoteFile);
        OutputStream os = new FileOutputStream("xx");
        StreamUtils.copy(is,os);
    }
}

import com.wacai.file.gateway.CapableFileManager;
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
public class FileManagerTest {

    static Logger log = LoggerFactory.getLogger(FileManagerTest.class);
//    //配置参数
    static String appKey = "3y3nmtkx3ykc";
    static String appSecret = "8cnukuk9tu7annnr";
    static String gatewayAuthUrl = "http://open-token-boot.loan.k2.test.wacai.info/token/auth";
    static String url = "http://file.test.wacai.info";
    static String namespace = "kd-dubbo";

//    //配置参数
//    static String appKey = "whb7mdd6kte3";
//    static String appSecret = "vt34mewq6hbhc433";
//    static String gatewayAuthUrl = "http://open.wacai.com/token/auth";
//    static String url = "http://file.wacai.com";
//    static String namespace = "kd-dubbo";

    public static void main(String args[]) throws IOException {

        CapableFileManager fileManager = new CapableFileManager(url,namespace,appKey,appSecret,gatewayAuthUrl);
        fileManager.setTimeout(1000);//超时时间
        fileManager.setExecTimes(2);//执行次数
        uploadOneFile(fileManager); //上传单个文件
        uploadFiles(fileManager); //上传多个文件
        Response<RemoteFile> response = uploadOneStream(fileManager);
        uploadOneStreams(fileManager); //以流的方式上传文件
        download(fileManager); //下载文件方式1
        downloadSecretkey(fileManager,response.getData().getSecretKey(),response.getData().getFilename()); //方式下载文件方式2
    }

    //上传单个文件
    private static void uploadOneFile(CapableFileManager fileManager) throws IOException {
        LocalFile localFile = new LocalFile();
        localFile.setFile(new File("D:\\1.txt"));
        localFile.setFilename("file.txt"); //上传到服务器时的名称
        Response<RemoteFile> response = fileManager.uploadFile(localFile);
        log.info("uploadOneFile response:{}",response);
    }

    private static Response<RemoteFile> uploadOneStream(CapableFileManager fileManager) throws IOException {
        StreamFile streamFile = new StreamFile();
        streamFile.setInputStream(new FileInputStream("D:\\1.txt"));
        streamFile.setFilename("stream.txt"); //上传到服务器时的名称
        Response<RemoteFile> response = fileManager.uploadStreamRetry(streamFile);
        log.info("response:{}",response);
        return response;
    }

    private static void uploadOneStreams(CapableFileManager fileManager) throws IOException {
        StreamFile streamFile = new StreamFile();
        streamFile.setInputStream(new FileInputStream("D:\\1.txt"));
        streamFile.setFilename("stream.txt"); //上传到服务器时的名称
        List<StreamFile> streamFiles = new ArrayList<>();
        streamFiles.add(streamFile);
        Response<List<RemoteFile>> response = fileManager.uploadStreamsRetry(streamFiles);
        log.info("response:{}",response);
    }

    private static void uploadFiles(CapableFileManager fileManager) throws IOException {
        List<LocalFile> localFileList = new ArrayList<>();

        LocalFile localFile = new LocalFile();
        localFile.setFile(new File("D:\\1.txt"));
        localFile.setFilename("1111.txt"); //上传多个文件时,此属性不起作用
        localFileList.add(localFile);

        LocalFile localFile2 = new LocalFile();
        localFile2.setFile(new File("D:\\2.txt"));
        localFile2.setFilename("2222.txt"); //上传多个文件时,此属性不起作用
        localFileList.add(localFile2);

        Response<List<RemoteFile>> response = fileManager.uploadFilesRetry(localFileList);
        log.info("responseList:{}",response);
    }

    private static void download(CapableFileManager fileManager) throws IOException {
        RemoteFile remoteFile = new RemoteFile("AAABY0qdPhGXfZRTVWuvl3StGPn0HhbM.txt",namespace);
        InputStream is = fileManager.downloadRetry(remoteFile);
        OutputStream os = new FileOutputStream("xx");
        StreamUtils.copy(is,os);
    }

    private static void downloadSecretkey(CapableFileManager fileManager,String appSecret,String filename) throws IOException {
        RemoteFile remoteFile = new RemoteFile(filename,namespace);
        remoteFile.setSecretKey(appSecret);
        InputStream is = fileManager.downloadSecretKeyRetry(remoteFile);
        OutputStream os = new FileOutputStream("secret");
        StreamUtils.copy(is,os);
    }
}

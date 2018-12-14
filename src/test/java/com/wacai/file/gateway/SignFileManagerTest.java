/**
 * Copyright 2009-2018 wacai.com.
 */
package com.wacai.file.gateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.wacai.file.gateway.entity.LocalFile;
import com.wacai.file.gateway.entity.RemoteFile;
import com.wacai.file.gateway.entity.Response;
import com.wacai.file.gateway.entity.StreamFile;

/**
 * 建议在使用上传下载接口之前使用setTimeout设置好超时时间，或者在构造实例时指定超时时间。
 * 建议您在服务中使用单实例。
 * 注意： 
 * 1. 如果是多实例使用SignFileManager，使用完之后，请使用destory方法销毁。建议您在服务中使用单实例。
 * 2. 使用setTimeout设置超时方法时，会重新初始化内部的client字段，此方法线程不安全。
 * 两个已知issue：
 * 1. 目前由于后台接口原因 还不支持批量上传时 指定文件名。
 * 2. 目前由于后台接口原因 批量上传时 目前返回的文件名顺序 跟您请求的时候 送的顺序 不保证一致。
 * @author yuyi@wacai.com
 * @since 2018-11-27
 *
 */
public class SignFileManagerTest {
	
//    private static final String FILE_GATEWAY_URL = "http://domino-file-gateway.platform-ci.k2.test.wacai.info";
	
	private static final String FILE_GATEWAY_URL = "http://172.16.72.245:8080";
	
	private static String appKey = "fsxd8a885fm8";
	
	private static String appSecret = "0f88a1b5ec034120bb6194119dc16359";
	
	@Test
	public void testUploadFileEmptyFileName() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile.setFile(uploadedFile);
		Response<RemoteFile> response = fileManager.uploadFile(localFile);
		assertEquals(0, response.getCode());
		assertNotNull(response.getData().getFilename());
		assertNotEquals("", response.getData().getFilename().trim());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(response.getData().getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}

	@Test
	public void testUploadFile() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile.setFile(uploadedFile);
		localFile.setFilename("test001.txt");
		Response<RemoteFile> response = fileManager.uploadFile(localFile);
		assertEquals(0, response.getCode());
		assertEquals("test001.txt", response.getData().getFilename());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(localFile.getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}
	
	@Test
	public void testUploadFileUseChineseFileName() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile.setFile(uploadedFile);
		localFile.setFilename("test我是中国人.txt");
		Response<RemoteFile> response = fileManager.uploadFile(localFile);
		assertEquals(0, response.getCode());
		assertEquals("test我是中国人.txt", response.getData().getFilename());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(localFile.getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}
	
	@Test
	public void testUploadStreamEmptyFileName() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		StreamFile streamFile = new StreamFile();
		streamFile.setInputStream(new FileInputStream(uploadedFile));
		fileManager.setTimeout(12000);
		Response<RemoteFile> response = fileManager.uploadStream(streamFile);
		assertEquals(0, response.getCode());
		assertNotNull(response.getData().getFilename());
		assertNotEquals("", response.getData().getFilename().trim());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(response.getData().getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}

	@Test
	public void testUploadStream() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		StreamFile streamFile = new StreamFile();
		streamFile.setFilename("test002.txt");
		streamFile.setInputStream(new FileInputStream(uploadedFile));
		Response<RemoteFile> response = fileManager.uploadStream(streamFile);
		assertEquals(0, response.getCode());
		assertEquals("test002.txt", response.getData().getFilename());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(streamFile.getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}
	
	@Test
	public void testUploadStreamUseChineseName() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		StreamFile streamFile = new StreamFile();
		streamFile.setFilename("test002我是中国人.txt");
		streamFile.setInputStream(new FileInputStream(uploadedFile));
		Response<RemoteFile> response = fileManager.uploadStream(streamFile);
		assertEquals(0, response.getCode());
		assertEquals("test002我是中国人.txt", response.getData().getFilename());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(streamFile.getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}
	
	@Test
	public void testUploadFilesEmptyFileName() throws IOException {
		for (int i = 0; i < 50; i++) {
			System.out.println("test-" + i);
			doTestUploadFilesEmptyFileName();
		}
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void doTestUploadFilesEmptyFileName() throws IOException, FileNotFoundException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile1 = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile1.setFile(uploadedFile);
		
		LocalFile localFile2 = new LocalFile();
		File uploadedFile2 = TestFileUtils.genTestFile();
		String md5OfUploadedFile2 = DigestUtils.md5Hex(new FileInputStream(uploadedFile2));
		localFile2.setFile(uploadedFile2);
		List<LocalFile> localFileList = new ArrayList<LocalFile>();
		localFileList.add(localFile1);
		localFileList.add(localFile2);
		Response<List<RemoteFile>> responses = fileManager.uploadFiles(localFileList);
		assertEquals(0, responses.getCode());
		assertEquals(2, responses.getData().size());
		assertNotNull(responses.getData().get(0).getFilename());
		assertNotEquals("", responses.getData().get(0).getFilename().trim());
		assertNotNull(responses.getData().get(1).getFilename());
		assertNotEquals("", responses.getData().get(1).getFilename().trim());
		
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(responses.getData().get(0).getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
//		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
		if (!md5OfUploadedFile.equals(md5OfDwonloadFile)) {
			if (md5OfUploadedFile2.equals(md5OfDwonloadFile)) {
				System.out.println("顺序不保证");
			} else {
				System.out.println("内容对不上");
			}
		}
		
		RemoteFile remoteFile2 = new RemoteFile();
		remoteFile2.setFilename(responses.getData().get(1).getFilename());
		remoteFile2.setNamespace("domino-test");
		InputStream donwloadInputstream2 = fileManager.download(remoteFile2);
		String md5OfDwonloadFile2 = DigestUtils.md5Hex(donwloadInputstream2);
		assertEquals(md5OfUploadedFile2, md5OfDwonloadFile2);
	}

	@Test
	public void testUploadFiles() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile1 = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile1.setFile(uploadedFile);
		localFile1.setFilename("test003.txt");
		
		LocalFile localFile2 = new LocalFile();
		File uploadedFile2 = TestFileUtils.genTestFile();
		String md5OfUploadedFile2 = DigestUtils.md5Hex(new FileInputStream(uploadedFile2));
		localFile2.setFile(uploadedFile2);
		localFile2.setFilename("test004.txt");
		List<LocalFile> localFileList = new ArrayList<LocalFile>();
		localFileList.add(localFile1);
		localFileList.add(localFile2);
		Response<List<RemoteFile>> responses = fileManager.uploadFiles(localFileList);
		assertEquals(0, responses.getCode());
		assertEquals(2, responses.getData().size());
		
		// 目前由于后台接口原因 还不支持批量上传时 指定文件名，目前返回的文件名顺序 对应你请求的时候 送的顺序
		assertNotEquals("test003.txt", responses.getData().get(0).getFilename());
		assertNotEquals("test004.txt", responses.getData().get(1).getFilename());
		
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(responses.getData().get(0).getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
		
		RemoteFile remoteFile2 = new RemoteFile();
		remoteFile2.setFilename(responses.getData().get(1).getFilename());
		remoteFile2.setNamespace("domino-test");
		InputStream donwloadInputstream2 = fileManager.download(remoteFile2);
		String md5OfDwonloadFile2 = DigestUtils.md5Hex(donwloadInputstream2);
		assertEquals(md5OfUploadedFile2, md5OfDwonloadFile2);
	}
	
	@Test
	public void testUploadFilesUseChineseName() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile1 = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile1.setFile(uploadedFile);
		localFile1.setFilename("test003我是中国人.txt");
		
		LocalFile localFile2 = new LocalFile();
		File uploadedFile2 = TestFileUtils.genTestFile();
		String md5OfUploadedFile2 = DigestUtils.md5Hex(new FileInputStream(uploadedFile2));
		localFile2.setFile(uploadedFile2);
		localFile2.setFilename("test004我是中国人.txt");
		List<LocalFile> localFileList = new ArrayList<LocalFile>();
		localFileList.add(localFile1);
		localFileList.add(localFile2);
		Response<List<RemoteFile>> responses = fileManager.uploadFiles(localFileList);
		assertEquals(0, responses.getCode());
		assertEquals(2, responses.getData().size());
		
		// 目前由于后台接口原因 还不支持批量上传时 指定文件名，目前返回的文件名顺序 对应你请求的时候 送的顺序
		assertNotEquals("test003我是中国人.txt", responses.getData().get(0).getFilename());
		assertNotEquals("test004我是中国人.txt", responses.getData().get(1).getFilename());
		
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(responses.getData().get(0).getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
		
		RemoteFile remoteFile2 = new RemoteFile();
		remoteFile2.setFilename(responses.getData().get(1).getFilename());
		remoteFile2.setNamespace("domino-test");
		InputStream donwloadInputstream2 = fileManager.download(remoteFile2);
		String md5OfDwonloadFile2 = DigestUtils.md5Hex(donwloadInputstream2);
		assertEquals(md5OfUploadedFile2, md5OfDwonloadFile2);
	}
	
	@Test
	public void testUploadStreamsEmptyFileName() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		StreamFile streamFile1 = new StreamFile();
		streamFile1.setInputStream(new FileInputStream(uploadedFile));
		
		File uploadedFile2 = TestFileUtils.genTestFile();
		String md5OfUploadedFile2 = DigestUtils.md5Hex(new FileInputStream(uploadedFile2));
		StreamFile streamFile2 = new StreamFile();
		streamFile2.setInputStream(new FileInputStream(uploadedFile2));
		
		List<StreamFile> streamFiles = new ArrayList<StreamFile>();
		
		streamFiles.add(streamFile1);
		streamFiles.add(streamFile2);
		Response<List<RemoteFile>> responses = fileManager.uploadStreams(streamFiles);
		assertEquals(0, responses.getCode());
		assertEquals(2, responses.getData().size());
		assertNotNull(responses.getData().get(0).getFilename());
		assertNotEquals("", responses.getData().get(0).getFilename().trim());
		assertNotNull(responses.getData().get(1).getFilename());
		assertNotEquals("", responses.getData().get(1).getFilename().trim());
		
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(responses.getData().get(0).getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
		
		RemoteFile remoteFile2 = new RemoteFile();
		remoteFile2.setFilename(responses.getData().get(1).getFilename());
		remoteFile2.setNamespace("domino-test");
		InputStream donwloadInputstream2 = fileManager.download(remoteFile2);
		String md5OfDwonloadFile2 = DigestUtils.md5Hex(donwloadInputstream2);
		assertEquals(md5OfUploadedFile2, md5OfDwonloadFile2);
	}

	@Test
	public void testUploadStreams() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		StreamFile streamFile1 = new StreamFile();
		streamFile1.setFilename("test007.txt");
		streamFile1.setInputStream(new FileInputStream(uploadedFile));
		
		File uploadedFile2 = TestFileUtils.genTestFile();
		String md5OfUploadedFile2 = DigestUtils.md5Hex(new FileInputStream(uploadedFile2));
		StreamFile streamFile2 = new StreamFile();
		streamFile2.setFilename("test008.txt");
		streamFile2.setInputStream(new FileInputStream(uploadedFile2));
		
		List<StreamFile> streamFiles = new ArrayList<StreamFile>();
		
		streamFiles.add(streamFile1);
		streamFiles.add(streamFile2);
		Response<List<RemoteFile>> responses = fileManager.uploadStreams(streamFiles);
		assertEquals(0, responses.getCode());
		assertEquals(2, responses.getData().size());
		
		// 目前由于后台接口原因 还不支持批量上传时 指定文件名，目前返回的文件名顺序 对应你请求的时候 送的顺序
		assertNotEquals("test007.txt", responses.getData().get(0).getFilename());
		assertNotEquals("test008.txt", responses.getData().get(1).getFilename());
		
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(responses.getData().get(0).getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
		
		RemoteFile remoteFile2 = new RemoteFile();
		remoteFile2.setFilename(responses.getData().get(1).getFilename());
		remoteFile2.setNamespace("domino-test");
		InputStream donwloadInputstream2 = fileManager.download(remoteFile2);
		String md5OfDwonloadFile2 = DigestUtils.md5Hex(donwloadInputstream2);
		assertEquals(md5OfUploadedFile2, md5OfDwonloadFile2);
	}
	
	@Test
	public void testUploadStreamsUseChineseName() throws IOException {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");// "stdout"为标准输出格式，"debug"为调试模式
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		File uploadedFile = TestFileUtils.genTestFile();
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		StreamFile streamFile1 = new StreamFile();
		streamFile1.setFilename("test007我是中国人.txt");
		streamFile1.setInputStream(new FileInputStream(uploadedFile));
		
		File uploadedFile2 = TestFileUtils.genTestFile();
		String md5OfUploadedFile2 = DigestUtils.md5Hex(new FileInputStream(uploadedFile2));
		StreamFile streamFile2 = new StreamFile();
		streamFile2.setFilename("test008我是中国人.txt");
		streamFile2.setInputStream(new FileInputStream(uploadedFile2));
		
		List<StreamFile> streamFiles = new ArrayList<StreamFile>();
		
		streamFiles.add(streamFile1);
		streamFiles.add(streamFile2);
		Response<List<RemoteFile>> responses = fileManager.uploadStreams(streamFiles);
		assertEquals(0, responses.getCode());
		assertEquals(2, responses.getData().size());
		
		// 目前由于后台接口原因 还不支持批量上传时 指定文件名，目前返回的文件名顺序 对应你请求的时候 送的顺序
		assertNotEquals("test007我是中国人.txt", responses.getData().get(0).getFilename());
		assertNotEquals("test008我是中国人.txt", responses.getData().get(1).getFilename());
		
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(responses.getData().get(0).getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
		
		RemoteFile remoteFile2 = new RemoteFile();
		remoteFile2.setFilename(responses.getData().get(1).getFilename());
		remoteFile2.setNamespace("domino-test");
		InputStream donwloadInputstream2 = fileManager.download(remoteFile2);
		String md5OfDwonloadFile2 = DigestUtils.md5Hex(donwloadInputstream2);
		assertEquals(md5OfUploadedFile2, md5OfDwonloadFile2);
	}
	
	@Test
	public void testUploadBigFile1() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		LocalFile localFile = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile(23 * 1024 * 1024);
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile.setFile(uploadedFile);
		localFile.setFilename("test001.txt");
		long start = System.currentTimeMillis();
		Response<RemoteFile> response = fileManager.uploadFile(localFile);
		System.out.println("Upload file used:" + (System.currentTimeMillis() - start));
		assertEquals(0, response.getCode());
		assertEquals("test001.txt", response.getData().getFilename());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(localFile.getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}
	
	@Test
	public void testUploadBigFile2() throws IOException {
		SignFileManager fileManager = new SignFileManager(FILE_GATEWAY_URL, "domino-test", appKey, appSecret);
		fileManager.setTimeout(120000);
		LocalFile localFile = new LocalFile();
		File uploadedFile = TestFileUtils.genTestFile(23 * 1024 * 1024);
		String md5OfUploadedFile = DigestUtils.md5Hex(new FileInputStream(uploadedFile));
		localFile.setFile(uploadedFile);
		localFile.setFilename("test001.txt");
		long start = System.currentTimeMillis();
		Response<RemoteFile> response = fileManager.uploadFile(localFile);
		System.out.println("Upload file used:" + (System.currentTimeMillis() - start));
		assertEquals(0, response.getCode());
		assertEquals("test001.txt", response.getData().getFilename());
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setFilename(localFile.getFilename());
		remoteFile.setNamespace("domino-test");
		InputStream donwloadInputstream = fileManager.download(remoteFile);
		String md5OfDwonloadFile = DigestUtils.md5Hex(donwloadInputstream);
		assertEquals(md5OfUploadedFile, md5OfDwonloadFile);
	}
}

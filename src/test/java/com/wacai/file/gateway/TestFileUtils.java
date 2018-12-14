/**
 * Copyright 2009-2018 wacai.com.
 */
package com.wacai.file.gateway;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

/**
 * @author yuyi@wacai.com
 * @since 2018-11-12
 *
 */
public class TestFileUtils {
	
	/**
	 * 生成测试用的文件
	 * @return 测试文件
	 */
	public static File genTestFile() {
		Random random = new Random();
        int filesize = random.nextInt(10240);
        return genTestFile(filesize);
	}
	
	public static File genTestFile(int filesize) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (filesize/10); i++) {
        	sb.append("ab12汉字中");
        }
        File testSamplePath = new File(System.getProperty("user.home") + "/domino-test");
        if (!testSamplePath.exists()) {
        	boolean createDirRes = testSamplePath.mkdirs();
            assertTrue("Mkdir failed：" + testSamplePath.getAbsolutePath(), createDirRes);
        }
		File sampleFile = new File(testSamplePath + "/sample-" + System.currentTimeMillis() + ".txt");
		if (sampleFile.exists()) {
			boolean deleteRes = sampleFile.delete();
			assertTrue("Delete sample file failed failed.", deleteRes);
		}
		BufferedWriter writer  = null;
		Writer out = null;
		try {
			out = new FileWriter(sampleFile);
			writer = new BufferedWriter(out);
			writer.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sampleFile;
	}

}

package com.wacai.file.token.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by fulushou on 2017/11/30.
 */
public class MD5Verify {

    private static MessageDigest messageDigest = null;
    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成字符串的md5校验值
     *
     * @param s
     * @return
     */
    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    public static String getMD5String(byte[] bytes) {
        messageDigest.update(bytes);
        return bufferToHex(messageDigest.digest());
    }

    /**
     * 生成文件的md5校验值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException {
        InputStream fis = new FileInputStream(file);
        return getStreamMD5String(fis);
    }

    /**
     * 生成文件流的md5校验值
     * @param fis
     * @return
     * @throws IOException
     */
    public static String getStreamMD5String(InputStream fis) throws IOException {

        try {
            byte[] buffer = new byte[1024 * 512];
            int numRead = 0;
            while ((numRead = fis.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, numRead);
            }
        }finally {
            fis.close();
        }
        return bufferToHex(messageDigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
        char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param md5 要校验的字符串
     * @param md5PwdStr 已知的md5校验码
     * @return
     */
    public static boolean checkPassword(String md5, String md5PwdStr) {
        return md5.equals(md5PwdStr);
    }

}

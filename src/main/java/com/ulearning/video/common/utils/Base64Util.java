package com.ulearning.video.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Base64Util {
    // 加密
    public static String enCodeBase64(String str) {
        return enCodeBase64(str, "utf-8");
    }

    // 加密
    public static String enCodeBase64(String str, String charset) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s = new BASE64Encoder().encode(b);
        }
        return s;
    }

    // 解密
    public static String deCodeBase64(String s) {
        return deCodeBase64(s, "utf-8");
    }

    // 解密
    public static String deCodeBase64(String s, String charset) {
        byte[] b = null;
        String result = null;
        if (s != null) {
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                b = decoder.decodeBuffer(s);
                result = new String(b, charset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
    /** 
     * @Title: GetImageStrFromUrl 
     * @Description: TODO(将一张网络图片转化成Base64字符串) 
     * @param imgURL 网络资源位置 
     * @return Base64字符串 
     */  
    public static String GetImageStrFromUrl(String imgURL) {  
    	ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            // 创建URL
            URL url = new URL(imgURL);
            byte[] by = new byte[1024];
            // 创建链接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream is = conn.getInputStream();
            // 将内容读取内存中
            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }
            // 关闭流
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data.toByteArray());
    }
}
package com.ulearning.video.utils;

import cn.hutool.core.util.StrUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 七牛帮助类
 */
@Slf4j
public class QiNiuUtil {
	/**
	 * 持久化操作进度响应
	 */
	/**
	 * 成功
	 */
	public static final int OPERATION_CODE_SUCCESS = 0;
	/**
	 * 等待处理
	 */
	public static final int OPERATION_CODE_WATING = 1;
	/**
	 * 处理中
	 */
	public static final int OPERATION_CODE_PROCESSING = 2;
	/**
	 * 处理失败
	 */
	public static final int OPERATION_CODE_FAIL = 3;
	/**
	 * 处理成功但通知失败
	 */
	public static final int OPERATION_CODE_SUCESS_NOTIFY_FAIL = 4;
	
	private static final String PIPELINE = "";
	
	public static final String QINIU_BUCKET = "";;
	
	private static Auth auth = null;

	private static Configuration config;
	
	static{
		initAuth();
	}
	
	private static void initAuth(){
		String accessKey = "";
		String secretKey = "";
		auth = Auth.create(accessKey, secretKey);
		if(QINIU_BUCKET.contains("asia") || QINIU_BUCKET.contains("net")){
			config = new Configuration(	Region.regionAs0());
		}else if(QINIU_BUCKET.contains("app") || QINIU_BUCKET.contains("pro")){
			config = new Configuration(Region.regionNa0());
		}else if (QINIU_BUCKET.contains("leicloud")){
			config = new Configuration(Region.region0());
		}else  {
			config = new Configuration();
		}
	}
	
	private static String getUpToken(String key){
		if(auth == null){
			initAuth();
		}
		return auth.uploadToken(QINIU_BUCKET, key, 3600, null);
	}
	
	public static Response uploadFile(String key, byte[] byteArr){
		UploadManager upload = new UploadManager(config);
		try {
//			deleteFile(key);
			return upload.put(byteArr, key, getUpToken(key));
		} catch (QiniuException e) {
			e.printStackTrace();
			return e.response;
		}
	}
	
	public static Response uploadFile(String key, File file){
		UploadManager upload = new UploadManager(config);
		try {
			return upload.put(file, key, getUpToken(key));
		} catch (QiniuException e) {
			e.printStackTrace();
			return e.response;
		}
	}
	
	public static void deleteFile(String key) {
		BucketManager bucketManager = new BucketManager(auth, config);
		try {
			bucketManager.delete(QINIU_BUCKET, key);
		} catch (QiniuException e) {
//			e.printStackTrace();
		}
	}
	
	public static FileInfo getFile(String key) throws QiniuException {
		BucketManager bucketManager = new BucketManager(auth, config);
		try {
			return bucketManager.stat(QINIU_BUCKET, key);
		} catch (QiniuException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static Boolean isFileExist(String key) throws QiniuException {
		BucketManager bucketManager = new BucketManager(auth, config);
		try {
			FileInfo fileInfo = bucketManager.stat(QINIU_BUCKET, key);
			if(fileInfo != null && fileInfo.hash != null){
				return true;
			}
		} catch (QiniuException e) {
			return false;
		}
		return false;
	}
	
	/**
	 * 执行持久化操作
	 */
	public static String document2pdf(String key){
		OperationManager operater = new OperationManager(auth, config);
		if(StrUtil.isBlank(key)){
			throw new IllegalArgumentException("没有传入文件key");
		}
		String saveasFileName = QINIU_BUCKET + ":" + key.split("\\.")[0] + ".pdf";
		StringMap params = new StringMap().putWhen("force", 1, true).putNotEmpty("pipeline", PIPELINE);
		try {
			String fops = "yifangyun_preview/v2|saveas/" + UrlSafeBase64.encodeToString(saveasFileName) ;
			String persistid = operater.pfop(QINIU_BUCKET, key, fops, params);
			return persistid;
		} catch (QiniuException e) {
			e.printStackTrace();
			log.error("操作发生错误, 响应码:" + e.response.statusCode + ",错误原因:" + e.response.error, e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 获取转换进度
	 */
	// public static String queryTransformProgress(String persistid){
	// 	// return HttpClientUtil.sendGet("http://api.qiniu.com/status/get/prefop", "id=" + persistid);
	// }
	
	/**
	 * 
	 * fetchRemote:抓取网络资源到空间 <br/>
	 *
	 * @author WangHuayong
	 * @param remoteSrcUrl
	 * @param key
	 * @return
	 * @throws QiniuException
	 * Date:2018-1-3下午02:27:31
	 * @since JDK 1.6
	 */
	public static Boolean fetchRemote(String remoteSrcUrl, String key){
		BucketManager bucketManager = new BucketManager(auth, config);
		try {
			 bucketManager.fetch(remoteSrcUrl, QINIU_BUCKET, key);
			 return true;
		} catch (QiniuException e) {
			return false;
		}
	}
	
}

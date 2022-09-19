package com.ulearning.video.utils.upload.huawei;

import cn.hutool.core.util.StrUtil;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.internal.utils.ServiceUtils;
import com.obs.services.model.*;
import com.ulearning.video.utils.upload.UploadService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 华为云OBS操作
 * MPC错误码表 https://support.huaweicloud.com/api-mpc/ErrorCode.html
 * */
public class HuaWeiObsServiceImpl implements UploadService {

	private String ak = HuaWeiObsConfig.ak;

	private String sk = HuaWeiObsConfig.sk;

	private String bucket = HuaWeiObsConfig.bucket;

	private String endPoint = HuaWeiObsConfig.endPoint;

	private ObsClient obsClient;

	@Override
	public String serverName() {
		return HuaWeiObsConfig.configured() ? "huawei" : null;
	}

	@PostConstruct
    private void init() {
		if (HuaWeiObsConfig.configured()) {
			//初始化obsClient
			ObsConfiguration config = new ObsConfiguration();
			config.setSocketTimeout(30000);
			config.setConnectionTimeout(10000);
			config.setEndPoint(endPoint);
			//初始化obsClient
			obsClient = new ObsClient(ak, sk, config);
		}
    }

	/**
	 * 上传文件
	 * @param path 文件在bucket中的路径（包含文件名）
	 * */
	@Override
	public String uploadFile(String path, File file){
		//通过临时访问密钥访问OBS，华为云接口文档 https://support.huaweicloud.com/perms-cfg-obs/obs_40_0008.html
//		ObsClient obsClient = new ObsClient("H4FPUBGL7MEWBMBKMB11",
//				"B9Rys3B3bavnjrPa3u9ANqobVWHWXuYUxFWaSFQq",
//				"ggpjbi1ub3J0aC00ScR7ImFjY2VzcyI6Ikg0RlBVQkdMN01FV0JNQktNQjExIiwibWV0aG9kcyI6WyJ0b2tlbiJdLCJwb2xpY3kiOnsiVmVyc2lvbiI6IjEuMSIsIlN0YXRlbWVudCI6W3siQWN0aW9uIjpbIm9iczpvYmplY3Q6UHV0T2JqZWN0Il0sIkVmZmVjdCI6IkFsbG93In1dfSwicm9sZSI6W10sInJvbGV0YWdlcyI6W10sInRpbWVvdXRfYXQiOjE2MzE2OTcxMDUzNzcsInVzZXIiOnsiZG9tYWluIjp7ImlkIjoiMDU5OTVkZmEzMzAwMGZiNjBmMmZjMDFhZTM5ZWZmODAiLCJuYW1lIjoidWxlYXJuaW5nQkoifSwiaWQiOiIwNTk5NWRmYjQ5MDAyNTZiMWZmN2MwMWEwMzUwYzFkNCIsIm5hbWUiOiJ1bGVhcm5pbmdCSiIsInBhc3N3b3JkX2V4cGlyZXNfYXQiOiIyMDIxLTA5LTI2VDA4OjU0OjUwLjAwMDAwMFoifX1L781N1K5l-jlshWyCLqocTz3mK2QjofQltF3HZ_oj8XYEF0-hXTL69iVheavKtYCclkVLwTT0XenItnzV0SX2Kvpa7x52hfRpmwE-ElEqeSmCbogir6CIArRQ041801h1g_scrHP2BwnVj_2ZP70ew056oHlhzjq8ELiZSn3ZQcd7dy5MH-OEpFiWjhMeKBwOju0joXlKr1ALWy07Yur8XLb5c-7l2QGUX7zrAUlOLC524tMU9fpE3Als_6JxTdGPh4CalOeNlTyGZCN5L92ZUDzsWo4pASxcUhSaYzgrCBl2m0iEM6-QMvPB4nHwOuL1_FamBg2L5iI5zJkQ7kIn",
//				endPoint);
		PutObjectResult putObjectResult = obsClient.putObject(bucket, path, file);
		if (200 == putObjectResult.getStatusCode()) {
			return putObjectResult.getObjectUrl();
		}
        return null;
    }

	@Override
	public String uploadFacePictureResource(File file, String fileName) {
		String path = uploadFile( "resources/" + fileName, file);
		if (StrUtil.isNotBlank(path)) {
			return processImage(path, fileName,"image/resize,m_lfit,w_295,h_413,limit_0/format,jpg");
		}
		return null;
	}


	/**
	 * 图片处理，生成处理后的图片
	 * @param path 文件在bucket中的路径（包含文件名）
	 * @param outputFileName 输出文件名（支持带文件夹路径）
	 * @param param 处理的参数
	 * 图片转码参数 https://support.huaweicloud.com/fg-obs/obs_01_0471.html
	 * 图片缩放参数 https://support.huaweicloud.com/fg-obs/obs_01_0441.html
	 * 处理图片持久化 https://support.huaweicloud.com/fg-obs/obs_01_0412.html
	 * */
	public String processImage(String path, String outputFileName, String param){
		TemporarySignatureRequest request = new TemporarySignatureRequest();
		request.setObjectKey(path);
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("x-image-process", param);
		queryParams.put("x-image-save-object", ServiceUtils.toBase64(outputFileName.getBytes(StandardCharsets.UTF_8)));
		queryParams.put("x-image-save-bucket", ServiceUtils.toBase64(bucket.getBytes(StandardCharsets.UTF_8)));
		request.setQueryParams(queryParams);
		request.setBucketName(bucket);
		//获取生成处理后的图片的命令url
		TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
		return response.getSignedUrl();
	}

}

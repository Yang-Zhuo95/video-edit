package com.ulearning.video.common.utils.upload.huawei;

import cn.hutool.core.util.StrUtil;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.mpc.v1.MpcClient;
import com.huaweicloud.sdk.mpc.v1.model.CreateExtractTaskReq;
import com.huaweicloud.sdk.mpc.v1.model.CreateExtractTaskRequest;
import com.huaweicloud.sdk.mpc.v1.model.CreateExtractTaskResponse;
import com.huaweicloud.sdk.mpc.v1.model.CreateTranscodingReq;
import com.huaweicloud.sdk.mpc.v1.model.CreateTranscodingTaskRequest;
import com.huaweicloud.sdk.mpc.v1.model.CreateTranscodingTaskResponse;
import com.huaweicloud.sdk.mpc.v1.model.MetaData;
import com.huaweicloud.sdk.mpc.v1.model.ObsObjInfo;
import com.huaweicloud.sdk.mpc.v1.model.VideoProcess;
import com.huaweicloud.sdk.mpc.v1.region.MpcRegion;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.internal.utils.ServiceUtils;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.oef.services.OefClient;
import com.oef.services.model.CreateAsyncFetchJobsRequest;
import com.oef.services.model.CreateAsynchFetchJobsResult;
import com.ulearning.video.common.utils.HttpUtil;
import com.ulearning.video.common.utils.upload.UploadService;
import com.ulearning.video.common.utils.upload.config.HuaWeiObsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 华为云OBS操作
 * MPC错误码表 https://support.huaweicloud.com/api-mpc/ErrorCode.html
 * */
@Service
public class HuaWeiObsServiceImpl implements UploadService {

	private static final String WORKING_DIR = "resources";

	private String ak;

	private String sk;

	private String bucket;

	private String endPoint;

	private String regionId;

	private String domain;

	private Integer templateM3u8;

	private String virtualPath;

	private ObsClient obsClient;

	private MpcClient mpcClient;

	private OefClient oefClient;

	private static Logger logger = LoggerFactory.getLogger(HuaWeiObsServiceImpl.class);

	Map<String, List<Integer>> transcodingTemplateMap = new HashMap<>();

	@Autowired
	private HuaWeiObsConfig huaWeiObsConfig;

	@Override
	public String serverName() {
		return huaWeiObsConfig.configured() ? "huawei" : null;
	}

	@PostConstruct
	private void init() {
		if (huaWeiObsConfig.configured()) {
			ak = huaWeiObsConfig.ak;
			sk = huaWeiObsConfig.sk;
			bucket = huaWeiObsConfig.bucket;
			endPoint = huaWeiObsConfig.endPoint;
			regionId = huaWeiObsConfig.regionId;
			domain = huaWeiObsConfig.domain;
			templateM3u8 = huaWeiObsConfig.templateM3u8;
			transcodingTemplateMap.put("mp4", Collections.singletonList(huaWeiObsConfig.templateMp4));
			transcodingTemplateMap.put("m3u8", Collections.singletonList(huaWeiObsConfig.templateM3u8));
			virtualPath = huaWeiObsConfig.virtualPath;
			ObsConfiguration config = new ObsConfiguration();
			config.setSocketTimeout(30000);
			config.setConnectionTimeout(10000);
			config.setEndPoint(endPoint);
			//初始化oefClient
			oefClient = new OefClient(ak, sk, config);
			//初始化obsClient
			obsClient = new ObsClient(ak, sk, config);
			ICredential basicCredentials = new BasicCredentials()
					.withAk(ak)
					.withSk(sk);
			mpcClient = MpcClient.newBuilder()
					.withCredential(basicCredentials)
					.withRegion(MpcRegion.valueOf(regionId))
					.build();
		}
    }

	/**
	 * 上传文件
	 * @param path 文件在bucket中的路径（包含文件名）
	 * */
	@Override
	public String uploadFile(String path, File file){
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

	@Override
	public String uploadVideo(String mp4Url) {
		logger.info("mp4Url：{} 文件上传华为云开始", mp4Url);
		String recordVideoUrl = "";
		String filePath = new File(mp4Url).getAbsolutePath();
		File file = new File(filePath);
		String key = "resources/" + System.currentTimeMillis() + UUID.randomUUID().toString() + file.getName();
		try {
			String path = this.uploadFile(key, file);
			if (StrUtil.isNotBlank(path)) {
				if (isVideoCodecSupported(path)) {
					createVideoTranscoding(path, transcodingTemplateMap.get("mp4"),
							Collections.singletonList(file.getName()));
				} else {
					logger.error("华为obs不支持的转码类型");
				}
				return path;
			}
		} catch (Exception e) {
			logger.error("upload error", e);
		}
		return null;
	}

	@Override
	public boolean configured() {
		return huaWeiObsConfig.configured();
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
		try { //访问这个url才能在obs生成处理后的图片，若不访问不会生成
			HttpUtil.doGet(response.getSignedUrl());
		} catch (Exception e) {
			e.printStackTrace();
			return path;
		}
		return path;
	}

	public boolean isVideoCodecSupported(String path) {
		MetaData metaData = getVideoInfo(path);
		if(null == metaData /*文件不存在*/||
				null == metaData.getVideo() || metaData.getVideo().isEmpty() /*文件无视频*/||
				null == metaData.getVideo().get(0).getCodec() ||
				metaData.getVideo().get(0).getCodec().equals("unsupport format")/*视频编码不支持*/ ||
				(null != metaData.getAudio() && !metaData.getAudio().isEmpty() /*文件无音频*/&&
						(null == metaData.getAudio().get(0).getCodec() ||
								metaData.getAudio().get(0).getCodec().equals("unsupport format")))/*音频编码不支持*/
		){
			return false;
		}
		return true;
	}

	public MetaData getVideoInfo(String path) {
		CreateExtractTaskRequest request = new CreateExtractTaskRequest();
		CreateExtractTaskReq body = new CreateExtractTaskReq();
		ObsObjInfo inputbody = new ObsObjInfo();
		inputbody.withBucket(bucket)
				.withLocation(regionId)
				.withObject(path);
		body.withSync(1);  //1同步（在response里直接返回结果） 2异步（只返回taskId，需要专门发请求查询结果）
		body.withInput(inputbody);
		request.withBody(body);
		try {
			CreateExtractTaskResponse response = mpcClient.createExtractTask(request);
			return response.getMetadata();
		} catch (ConnectionException | RequestTimeoutException | ServiceResponseException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 新建视频转码任务
	 * 华为云接口文档 https://support.huaweicloud.com/api-mpc/mpc_04_0017.html
	 * 自定义转码模板的文档 https://support.huaweicloud.com/usermanual-mpc/mpc010008.html
	 * @param path 视频文件在bucket中的路径（包含文件名）
	 * @param templateIdList 转码模板id列表，必须是同一个转码模板组内的模板id才行，否则会报错
	 * @param outputFileNameList 输出文件名列表，个数要和templateIdList一样多才行，如果不指定则由华为云默认命名，如aaa_H.264_720x1280_HEAAC_1000.mp4
	 * @return taskId
	 * */
	public Integer createVideoTranscoding(String path, List<Integer> templateIdList, List<String> outputFileNameList) {
		// 把response.getTaskId()存到数据库，在定时任务中取出来查询是否转码成功，
		// 如果转码失败了需要重新转码，重新转码失败了可以查出来这个任务的taskId
		return doCreateVideoTranscoding(path, templateIdList, outputFileNameList);
	}

	public Integer doCreateVideoTranscoding(String path, List<Integer> templateIdList, List<String> outputFileNameList) {
		//输出文件夹，就按照输入的文件夹
		int index = path.lastIndexOf("/");
		String outputPath = WORKING_DIR;
		if(-1 != index){
			outputPath = path.substring(0, index);
		}
		int m3u8Index = templateIdList.indexOf(templateM3u8);
		if(-1 != m3u8Index){
			//生成m3u8的封面
			createFetch(domain + "/" + path + "?vframe/jpg/offset/1", outputPath + "/" + outputFileNameList.get(m3u8Index) + ".jpg");
		}
		CreateTranscodingTaskRequest request = new CreateTranscodingTaskRequest();
		CreateTranscodingReq body = new CreateTranscodingReq();
		//1开启上采样 0禁止上采样 上采样是指用低分辨率的视频转码为高分辨率的视频，如果禁止的话，不会生成超过原视频分辨率的视频
		VideoProcess videoProcessbody = new VideoProcess();
		videoProcessbody.withUpsample(1);
		//转码后输出文件夹
		ObsObjInfo outputbody = new ObsObjInfo();
		outputbody.withBucket(bucket)
				.withLocation(regionId)
				.withObject(outputPath);
		//输入
		ObsObjInfo inputbody = new ObsObjInfo();
		inputbody.withBucket(bucket)
				.withLocation(regionId)
				.withObject(path);
		body.withVideoProcess(videoProcessbody);
		if(null != outputFileNameList && !outputFileNameList.isEmpty()){
			body.withOutputFilenames(outputFileNameList);
		}
		//转码模板
		body.withTransTemplateId(templateIdList);
		body.withOutput(outputbody);
		body.withInput(inputbody);
		request.withBody(body);
		try {
			CreateTranscodingTaskResponse response = mpcClient.createTranscodingTask(request);
			return response.getTaskId();
		} catch (ConnectionException | RequestTimeoutException | ServiceResponseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 抓取网络资源到obs
	 * @param url 源文件url
	 * @param path 文件在bucket中的路径（包含文件名）
	 * */
	public String createFetch(String url, String path) {
		// 初始化异步抓取策略
		//xxx 为 IAM 委托名
//		PutExtensionPolicyRequest request = new PutExtensionPolicyRequest();
//		request.setFetch(new FetchBean("open","xxx"));
		//为桶配置异步抓取策略
//		oefClient.putExtensionPolicy(bucket, request);
//		oefClient.close();

		// 创建异步抓取任务
		CreateAsyncFetchJobsRequest request = new CreateAsyncFetchJobsRequest(url, bucket);
		request.setObjectKey(path);
//		request.setCallBackUrl();
		CreateAsynchFetchJobsResult result = oefClient.createFetchJob(request);
//		try {
//			oefClient.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return result.getId();
	}

}

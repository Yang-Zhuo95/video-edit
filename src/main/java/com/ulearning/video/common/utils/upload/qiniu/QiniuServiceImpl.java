package com.ulearning.video.common.utils.upload.qiniu;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import com.ulearning.video.common.utils.Base64Util;
import com.ulearning.video.common.utils.HttpUtil;
import com.ulearning.video.common.utils.upload.UploadService;
import com.ulearning.video.common.utils.upload.config.QiniuConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QiniuServiceImpl implements UploadService {

    Set<String> videoSupportedSet = new HashSet<String>(Arrays.asList(".avi", ".3gp", ".rmvb"
            , ".wmv", ".mkv", ".asf", ".mov", ".mpeg", ".mpg", ".f4v", ".flv", ".vob", ".rm"
            , ".divx", ".rmvb", ".mp4"));

    private static Logger logger = LoggerFactory.getLogger(QiniuServiceImpl.class);

    private String QINIU_BUCKET;
    private String QINIU_ACCESS_KEY;
    private String QINIU_SECRET_KEY;
    private String QINIU_PIPELINE;
    private String QINIU_DOWNLOAD_URL;
    private String QINIU_VIRTUAL_PATH;

    @Autowired
    private QiniuConfig qiniuConfig;

    private Auth auth = null;

    @Override
    public String serverName() {
        return qiniuConfig.configured() ? "qiniu" : null;
    }

    @PostConstruct
    private void init() {
        if (qiniuConfig.configured()) {
            QINIU_BUCKET = qiniuConfig.bucket;
            QINIU_ACCESS_KEY = qiniuConfig.accessKey;
            QINIU_SECRET_KEY = qiniuConfig.secretkey;
            QINIU_PIPELINE = qiniuConfig.pipeline;
            QINIU_DOWNLOAD_URL = qiniuConfig.download;
            QINIU_VIRTUAL_PATH = qiniuConfig.virtualpath;
            auth = Auth.create(QINIU_ACCESS_KEY, QINIU_SECRET_KEY);
        }
    }

    private void initAuth() {
        auth = Auth.create(QINIU_ACCESS_KEY, QINIU_SECRET_KEY);
    }

    private String getUpToken(String key) {
        if (auth == null) {
            initAuth();
        }
        return auth.uploadToken(QINIU_BUCKET, key, 3600, null);
    }

    public Auth getAuth() {
        if (auth == null) {
            initAuth();
        }
        return auth;
    }

    public Response uploadFile(String key, byte[] byteArr) {
        UploadManager upload = new UploadManager(null);
        try {
            return upload.put(byteArr, key, getUpToken(key));
        } catch (QiniuException e) {
            e.printStackTrace();
            return e.response;
        }
    }

    public Response upload(String key, File file) {
        UploadManager upload = new UploadManager(new Configuration());
        try {
            return upload.put(file, key, getUpToken(key));
        } catch (QiniuException e) {
            e.printStackTrace();
            return e.response;
        }
    }

    @Override
    public String uploadFile(String key, File file) {
        return upload(key, file).isOK() ? QINIU_DOWNLOAD_URL + key : null;
    }

    @Override
    public String uploadFacePictureResource(File file, String fileName/*在qiniu的文件名*/) {
        UploadManager uploadManager = new UploadManager(new Configuration());
        boolean isOK = false;
        try {
            String saveMp4Entry = String.format("%s:" + fileName, QINIU_BUCKET);
            String persistentOpfs = String.format("imageMogr2/thumbnail/295x413/format/jpg/blur/1x0/quality/75|saveas/%s", UrlSafeBase64.encodeToString(saveMp4Entry));
            StringMap params = new StringMap();
            params.putWhen("force", 1, true);
            params.putNotEmpty("pipeline", QINIU_PIPELINE);
            Response response = uploadManager.put(file, fileName, getUpToken(fileName));
            isOK = response.isOK();
            System.out.println(response.toString());
            fops(fileName, persistentOpfs, params);
        } catch (QiniuException e) {
            isOK = false;
            e.printStackTrace();
        }
        return isOK ? QINIU_DOWNLOAD_URL + fileName : null;
    }

    @Override
    public String uploadVideo(String mp4Url) {
        logger.info("mp4Url：{} 文件上传七牛云开始", mp4Url);
        String recordVideoUrl = "";
        String filePath = new File(mp4Url).getAbsolutePath();
        File file = new File(filePath);
        String key = "resources/" + System.currentTimeMillis() + UUID.randomUUID().toString() + file.getName();
        String sourceMp4 = QINIU_VIRTUAL_PATH + key;
        try {
            Response re = this.upload(key, file);
            if (re.isOK()) {
                logger.info("入参mp4Url:{}, 上传七牛云返回原视频地址：{}", mp4Url, sourceMp4);

                // 获取七牛云的视频编码格式
                String avinfoReuslt = HttpUtil.doGet(sourceMp4 + "?avinfo");
                JSONObject avinfoObject = JSONObject.parseObject(avinfoReuslt);
                List<Object> avinfoObjectList = (List<Object>) avinfoObject.get("streams");
                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(avinfoObjectList.get(0)));
                String codecName = (String) jsonObject.get("codec_name");
                if (codecName != null && codecName.equals("h264")) {
                    return sourceMp4;
                }

                // 对H265格式视频转码
                String enCodeKey = Base64Util.enCodeBase64(key);
                JSONObject object = transAndRename(QINIU_BUCKET, enCodeKey);
                logger.info("入参mp4Url:{},七牛云视频转码结果 object :{}", mp4Url, object);
                if ((Boolean) object.get("success")) {
                    String infoMp4Url = (String) object.get("info");
                    recordVideoUrl = QINIU_VIRTUAL_PATH + infoMp4Url;
                } else {
                    logger.error("七牛云视频转码异常, 入参mp4Url:{}", mp4Url);
                }
                logger.info("入参mp4Url:{}, 七牛云视频转码后视频地址:{} ", mp4Url, recordVideoUrl);
                return recordVideoUrl;
            }
        } catch (Exception e) {
            logger.error("upload error", e);
            return sourceMp4;
        }
        return recordVideoUrl;
    }

    @Override
    public boolean configured() {
        return qiniuConfig.configured();
    }


    public JSONObject transAndRename(String bucket, String mediaUrlEncoded) throws QiniuException {
        JSONObject resultJson = new JSONObject();
        Auth auth = this.getAuth();
        Configuration cfg = new Configuration(Zone.zone0());
        String mediaKey = new String(UrlSafeBase64.decode(mediaUrlEncoded));

        if(mediaKey.lastIndexOf(".") < 0 || !videoSupportedSet.contains(mediaKey.substring(mediaKey.lastIndexOf(".")).toLowerCase())){
            resultJson.put("success", false);
            resultJson.put("info", "not a video file! Support:" + videoSupportedSet);
            return resultJson;
        }else{
            String mp4KeyPc = mediaKey;
            if(mediaKey.contains(".")){
                mp4KeyPc = mediaKey.substring(0, mediaKey.lastIndexOf(".")) + "_convert.mp4";

            }
            String saveMp4Entry = String.format("%s:" + mp4KeyPc, bucket);
            String avthumbMp4Fop = String.format("avthumb/mp4/vb/800k/vcodec/libx264/acodec/aac/s/640x360/autoscale/0|saveas/%s", UrlSafeBase64.encodeToString(saveMp4Entry));
            String persistentOpfs = StringUtils.join(new String[]{
                    avthumbMp4Fop
            }, ";");

            String persistentPipeline = QINIU_PIPELINE;

            OperationManager operationManager = new OperationManager(auth, cfg);
            OperationStatus operationStatus = new OperationStatus();
            try {
                String persistentId = operationManager.pfop(bucket, mediaKey, persistentOpfs, persistentPipeline, false);
                //可以根据该 persistentId 查询任务处理进度
                operationStatus = operationManager.prefop(persistentId);

                for(int i = 0; i < 10 * 60; i++){
                    operationStatus = operationManager.prefop(persistentId);
                    if(operationStatus.code == 0){
                        resultJson.put("success", true);
                        resultJson.put("info", mp4KeyPc);
                        return resultJson;
                    }
                    Thread.sleep(500);
                }


                resultJson.put("success", false);
                resultJson.put("info", "timeout!");
                return resultJson;
            } catch (InterruptedException e) {
                resultJson.put("success", false);
                resultJson.put("info", operationStatus.desc);
                return resultJson;
            }
        }
    }

    public boolean fops(String key, String fops, StringMap params) {
        try {
            // 针对指定空间的文件触发 pfop 操作
            Auth auth = Auth.create(QINIU_ACCESS_KEY, QINIU_SECRET_KEY);
            OperationManager operater = new OperationManager(auth, new Configuration());
            String id = operater.pfop(QINIU_BUCKET, key, fops, params);
            // 可通过下列地址查看处理状态信息。
            // 实际项目中设置 notifyURL，接受通知。通知内容和处理完成后的查看信息一致。
            //String url = "http://api.qiniu.com/status/get/prefop?id=" + id;

            //可以根据该 persistentId 查询任务处理进度
            System.out.println(id);
            OperationStatus operationStatus = operater.prefop(id);
            System.out.println(operationStatus.id);
        } catch (QiniuException e) {
            // 请求失败时简单状态信息
            System.out.println("操作发生错误, 响应码:" + e.response.statusCode + ",错误原因:" + e.response.error);
            return false;
        }
        return true;
    }

    public boolean deleteFile(String fileUrl) {
        Auth auth = Auth.create(QINIU_ACCESS_KEY, QINIU_SECRET_KEY);
        BucketManager bucketManager = new BucketManager(auth, new Configuration());
        String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        boolean isOK = false;
        try {
            Response delete = bucketManager.delete(QINIU_BUCKET, key);
            isOK = delete.isOK();
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return isOK;
    }
}

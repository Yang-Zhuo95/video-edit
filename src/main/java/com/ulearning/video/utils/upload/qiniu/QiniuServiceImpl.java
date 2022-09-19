package com.ulearning.video.utils.upload.qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.processing.OperationStatus;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import com.ulearning.video.ffmpeg.config.FfmPegMergeConfig;
import com.ulearning.video.utils.upload.UploadService;

import java.io.File;

public class QiniuServiceImpl implements UploadService {

    private String QINIU_BUCKET = QiniuConfig.BUCKET;
    private String QINIU_ACCESS_KEY = QiniuConfig.ACCESSKEY;
    private String QINIU_SECRET_KEY = QiniuConfig.SECRETKEY;
    private String QINIU_PIPELINE = QiniuConfig.PIPELINE;
    private String QINIU_DOWNLOAD_URL = QiniuConfig.DOWNLOAD;

    private Auth auth = null;

    @Override
    public String serverName() {
        System.out.println(FfmPegMergeConfig.PERIOD);
        System.out.println(FfmPegConfig.REJECT_POLICY);
        System.out.println(QiniuConfig.ACCESSKEY);
        return QiniuConfig.configured() ? "qiniu" : null;
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

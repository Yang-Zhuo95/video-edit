package com.ulearning.video.common.utils.upload.local;

import cn.hutool.core.img.Img;
import cn.hutool.core.io.FileUtil;
import com.ulearning.video.common.utils.upload.UploadService;
import com.ulearning.video.common.utils.upload.config.LocalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author yangzhuo
 * @description 本地上传工具
 * @date 2021/10/11 11:29
 */
@Service
public class LocalServiceImpl implements UploadService {

    private String filePath;
    private String localDownloadUrl;

    @Autowired
    private LocalConfig localConfig;

    @PostConstruct
    private void init() {
        filePath = localConfig.filePath;
        localDownloadUrl = localConfig.localDownloadUrl;
    }

    @Override
    public String serverName() {
        return localConfig.configured() ? "local" : null;
    }

    // 上传文件
    @Override
    public String uploadFile(String key, File file) {
        String localFilePath = getLocalFilePath(key);
        try {
            FileUtil.copy(file, new File(localFilePath), false);
        } catch (Exception e) {
            return null;
        }
        return getLocalDownloadUrl(key);
    }

    // 上传头像
    @Override
    public String uploadFacePictureResource(File file, String fileName) {
        String localFilePath = getLocalFilePath(fileName);
        // 对图片进行压缩上传
        try {
            Img.from(file)
                    .setQuality(0.6)
                    .write(FileUtil.file(localFilePath));
        } catch (Exception e) {
            return null;
        }
        return getLocalDownloadUrl(fileName);
    }

    @Override
    public String uploadVideo(String mp4Url) {
        throw new IllegalArgumentException("本地上传,不支持该方法");
    }

    @Override
    public boolean configured() {
        return localConfig.configured();
    }

    private String getLocalFilePath(String filePath) {
        return this.filePath + File.separator + filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private String getLocalDownloadUrl(String filePath) {
        return localDownloadUrl + "" + filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}

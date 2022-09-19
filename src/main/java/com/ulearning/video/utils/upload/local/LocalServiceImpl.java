package com.ulearning.video.utils.upload.local;

import cn.hutool.core.img.Img;
import cn.hutool.core.io.FileUtil;
import com.ulearning.video.utils.upload.UploadService;

import java.io.File;

/**
 * @description 本地上传工具
 * @date 2021/10/11 11:29
 * @author yangzhuo
 */
public class LocalServiceImpl implements UploadService {
    private String FILE_PATH = LocalConfig.filePath;
    private String localDownloadUrl = LocalConfig.localDownloadUrl;

    @Override
    public String serverName() {
        return LocalConfig.configured() ? "local" : null;
    }

    // 上传文件
    @Override
    public String uploadFile(String key, File file) {
        String localFilePath = getLocalFilePath(key);
        try {
            FileUtil.copy(file,new File(localFilePath),false);
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

    private String getLocalFilePath(String filePath) {
        return FILE_PATH + File.separator + filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private String getLocalDownloadUrl(String filePath) {
        return localDownloadUrl + "" + filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}

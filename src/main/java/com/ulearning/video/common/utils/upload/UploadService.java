package com.ulearning.video.common.utils.upload;

import java.io.File;

/**
 * @author yangzhuo
 * @description 上传服务
 * @date 2022-09-19 14:57
 */
public interface UploadService {

    /**
     * 获取服务名
     * @return String
     * @date 2022/9/19 15:09
     * @author yangzhuo
     */
    String serverName();

    /**
     * 上传文件
     * @param key  文件相对路径
     * @param file 文件
     * @return String   文件下载地址
     * @date 2022/9/19 15:03
     * @author yangzhuo
     */
    String uploadFile(String key, File file);

    /**
     * 上传文件
     * @param file 文件
     * @param path  文件相对路径
     * @return String   文件下载地址
     * @date 2022/9/19 15:03
     * @author yangzhuo
     */
    String uploadFacePictureResource(File file, String path);

    /**
     * 上传视频文件并对视频文件编码
     * @param mp4Url    视频文件 本地地址
     * @return String   文件下载地址
     * @date 2022/9/19 15:03
     * @author yangzhuo
     */
    String uploadVideo(String mp4Url);

    /**
     * 服务的配置信息是否完整
     * @return boolean  false-不完整 true-完整
     * @date 2022/9/19 15:03
     * @author yangzhuo
     */
    boolean configured();
}

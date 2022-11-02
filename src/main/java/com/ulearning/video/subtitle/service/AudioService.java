package com.ulearning.video.subtitle.service;

import com.ulearning.video.subtitle.fo.SubtitleFo;
import com.ulearning.video.subtitle.model.AudioSubtitleModel;

/**
 * @author yangzhuo
 * @description 音频服务
 * @date 2022-09-21 11:52
 */
public interface AudioService {

    /**
     * 创建提取字幕异步任务
     * @param subtitleFo 字幕提取任务表单类
     * @return Integer      异步任务id
     * @date 2022/9/22 9:17
     * @author yangzhuo
     */
    Integer subtitleTask(SubtitleFo subtitleFo);

    /**
     * 从视频中抽取音频文件
     * @param videoPath 源视频地址
     * @param audioPath 输出音频路径
     * @return Integer 0-成功 1-失败
     * @date 2022/9/21 16:35
     * @author yangzhuo
     */
    Integer videoToAudio(String videoPath, String audioPath);

    /**
     * 创建音频转字幕任务
     * @param audioPath 音频网络地址(必须是网络地址)
     * @param callback  执行任务的回调地址
     * @return Integer   音频转字幕任务id
     * @date 2022/9/21 16:35
     * @author yangzhuo
     */
    String audioSubtitle(String audioPath, String callback);

    /**
     * 根据字幕转换任务信息, 回查转换结果
     * @param task 字幕转换任务信息
     * @date 2022/9/28 10:22
     * @author yangzhuo
     */
    void saveSubtitle(AudioSubtitleModel task);

}

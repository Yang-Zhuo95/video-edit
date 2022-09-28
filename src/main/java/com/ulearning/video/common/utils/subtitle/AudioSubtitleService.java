package com.ulearning.video.common.utils.subtitle;

import com.alibaba.fastjson.JSONObject;
import com.ulearning.video.common.utils.subtitle.eneity.Result;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleObject;

/**
 * @author yangzhuo
 * @description 音频转字幕服务
 * @date 2022-09-21 17:09
 */
public interface AudioSubtitleService {

    /**
     * 获取服务名
     * @return String
     * @date 2022/9/19 15:09
     * @author yangzhuo
     */
    String serverName();

    /**
     * 服务的配置信息是否完整
     * @return boolean  false-不完整 true-完整
     * @date 2022/9/19 15:03
     * @author yangzhuo
     */
    boolean configured();

    /**
     * 创建音频转字幕任务
     * @param audioPath 音频网络地址(必须是网络地址)
     * @param callback  执行任务的回调地址
     * @return String   任务id
     * @date 2022/9/21 16:35
     * @author yangzhuo
     */
    String audioSubtitle(String audioPath, String callback);

    /**
     * 查询字幕生成任务
     * @param taskId 音频网络地址(必须是网络地址)
     * @return Result   执行结果
     * @date 2022/9/21 16:35
     * @author yangzhuo
     */
    Result queryTask(String taskId);
}

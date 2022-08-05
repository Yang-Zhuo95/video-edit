package com.ulearning.video.editor.service;

import com.ulearning.video.editor.fo.CatchPictureFo;
import com.ulearning.video.editor.fo.MultipleMergeFo;
import com.ulearning.video.ffmpeg.entity.ProgressInfo;
import com.ulearning.video.ffmpeg.entity.TaskInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yangzhuo
 * @description 视频编辑服务类
 * @date 2022-07-29 11:09
 */
public interface VideoEditService {
    /**
     * 视频合成
     * @param multipleMergeFo 视频合成表单类
     * @return TaskInfo 任务信息
     * @date 2022/7/29 11:16
     * @author yangzhuo
     */
    TaskInfo multipleMerge(MultipleMergeFo multipleMergeFo);

    /**
     * 查询任务执行进度
     * @param taskId 任务id
     * @return ProgressInfo 进度信息
     * @date 2022/7/29 11:16
     * @author yangzhuo
     */
    ProgressInfo getExecuteProgress(Integer taskId);

    /**
     * 抓取图片并返回
     * @param catchPictureFo 抓取图片表单类
     * @param resp           响应体
     * @date 2022/8/5 11:46
     * @author yangzhuo
     */
    void catchPicture(CatchPictureFo catchPictureFo, HttpServletResponse resp) throws IOException;
}

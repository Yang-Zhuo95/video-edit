package com.ulearning.video.editor.service.impl;

import com.ulearning.video.common.enums.ContentTypeEnum;
import com.ulearning.video.common.exception.CustomizeException;
import com.ulearning.video.editor.fo.CatchPictureFo;
import com.ulearning.video.ffmpeg.actuator.Actuator;
import com.ulearning.video.ffmpeg.actuator.CatchPictureActuator;
import com.ulearning.video.ffmpeg.actuator.MultipleMergeActuator;
import com.ulearning.video.ffmpeg.cache.FfmPegCache;
import com.ulearning.video.ffmpeg.dao.VideoEditRecordDao;
import com.ulearning.video.editor.fo.MultipleMergeFo;
import com.ulearning.video.editor.service.VideoEditService;
import com.ulearning.video.ffmpeg.entity.ProgressInfo;
import com.ulearning.video.ffmpeg.entity.TaskInfo;
import com.ulearning.video.ffmpeg.executor.FfmPegExecutor;
import com.ulearning.video.ffmpeg.executor.FfmPegMergeExecutor;
import com.ulearning.video.ffmpeg.model.VideoEditRecordModel;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;
import com.ulearning.video.common.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 视频编辑服务实现类
 * @date 2022-07-29 11:10
 */
@Slf4j
@Service
public class VideoEditServiceImpl implements VideoEditService {

    @Resource
    private VideoEditRecordDao videoEditRecordDao;

    @Override
    public TaskInfo multipleMerge(MultipleMergeFo multipleMergeFo) {
        // 视频合成
        return FfmPegExecutor.createCmdAndExecute(new MultipleMergeActuator(multipleMergeFo));
    }

    @Override
    public ProgressInfo getExecuteProgress(Integer taskId) {
        // TODO 判断ip地址是否与本机一致

        // 查询缓存中的任务信息
        TaskInfo taskInfo = FfmPegCache.getTaskInfo(taskId);
        // 不存在缓存就去mysql中查询任务信息
        if (Objects.isNull(taskInfo)) {
            VideoEditRecordModel info = videoEditRecordDao.findById(taskId);
            if (Objects.isNull(info)) {
                log.error("taskId: {}, 任务信息不存在", taskId);
                return null;
            }
            // 其它状态(非等待/执行状态)
            return ProgressInfo.builder()
                    .taskId(taskId).msg(info.getMsg())
                    .outputPath(VideoEditRecordModel.SUCCESS.equals(info.getStatus()) ? info.getPath() : null)
                    .status(info.getStatus())
                    .needRetry("create".equals(info.getMsg()))
                    .build();

        }

        // 查询缓存中的执行进度信息
        MutablePair<String, String> progress = FfmPegCache.getProgress(taskId);
        // 未开始,获取任务在队列中的位置
        if (Objects.isNull(progress)) {
            // 队列完成总数
            long completedTaskCount = FfmPegExecutor.getCompletedTaskCount();
            // 获取任务创建时,已完成总数
            Long oldCompletedTaskCount = taskInfo.getCompletedTaskCount();
            // 正在进行的任务数
            int activeCount = FfmPegExecutor.getActiveCount();
            // 当前等待位置
            Long waitIdx = taskInfo.getWaitCount() - completedTaskCount - oldCompletedTaskCount;
            return ProgressInfo.builder()
                    .taskId(taskId).waitIdx(waitIdx)
                    .activeCount(activeCount)
                    .status(VideoEditRecordModel.NOT_STARTED)
                    .needRetry(false)
                    .build();
        } else { // 执行中,获取执行进度信息
            return ProgressInfo.builder()
                    .taskId(taskId).totalDuration(progress.getLeft())
                    .duration(progress.getRight() + '0').status(VideoEditRecordModel.STARTED)
                    .needRetry(false)
                    .build();
        }
    }

    @Override
    public void catchPicture(CatchPictureFo catchPictureFo, HttpServletResponse resp) throws IOException {
        Long duration = catchPictureFo.getDuration();
        catchPictureFo.setDuration(Objects.nonNull(duration) && duration.compareTo(0L) >= 0 ? duration : 1);
        boolean flag = FfmPegCache.containsFileKey(catchPictureFo);
        File tempFile = FfmPegCache.getFile(catchPictureFo);
        Integer result = 1;
        if (!flag) {
            // 合并请求并延迟返回,减少并发请求导致重复执行命令
            Actuator actuator = new CatchPictureActuator(catchPictureFo);
            result = FfmPegMergeExecutor.execute(actuator);
            tempFile = actuator.getOutputFile();
        } else if (Objects.nonNull(tempFile) && tempFile.exists()) {
            result = 0;
        }
        if (FfmpegUtil.CODE_SUCCESS.equals(result)) {
            ResponseUtil.writeFileToResponse(tempFile, ContentTypeEnum.getContentTypeByFile(tempFile), "utf-8",resp);
        } else {
            throw new CustomizeException("图片截取失败");
        }
    }

    @Override
    public boolean cancelTask(Integer taskId) {
        return videoEditRecordDao.updateStatus(taskId, VideoEditRecordModel.NOT_STARTED, VideoEditRecordModel.STOPPED) == 1;
    }

}

package com.ulearning.video.editor.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.ulearning.video.common.exception.DataInconsistentException;
import com.ulearning.video.editor.fo.CatchPictureFo;
import com.ulearning.video.ffmpeg.actuator.MultipleMergeActuator;
import com.ulearning.video.ffmpeg.cache.FfmPegCache;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import com.ulearning.video.ffmpeg.dao.VideoEditRecordDao;
import com.ulearning.video.editor.fo.MultipleMergeFo;
import com.ulearning.video.editor.service.VideoEditService;
import com.ulearning.video.ffmpeg.entity.ProgressInfo;
import com.ulearning.video.ffmpeg.entity.TaskInfo;
import com.ulearning.video.ffmpeg.executor.FfmPegExecutor;
import com.ulearning.video.ffmpeg.model.VideoEditRecordModel;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 视频编辑服务实现类
 * @date 2022-07-29 11:10
 */
@Slf4j
@Service
public class VideoEditServiceImpl implements VideoEditService {

    private VideoEditRecordDao videoEditRecordDao;

    @Resource
    public void setVideoEditRecordDao(VideoEditRecordDao videoEditRecordDao) {
        this.videoEditRecordDao = videoEditRecordDao;
    }

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
        duration = Objects.nonNull(duration) && duration.compareTo(0L) >= 0 ? duration : 1;
        File tempFile = FfmPegCache.getFile(catchPictureFo);
        Integer result;
        if (Objects.isNull(tempFile)) {
            tempFile = new File(FfmPegConfig.WORK_SPACE + System.currentTimeMillis() + "-" + IdUtil.simpleUUID() + ".png");
            tempFile.deleteOnExit();
            result = FfmpegUtil.catchJpg(catchPictureFo.getSource(), tempFile.getAbsolutePath(),
                    duration.toString(), catchPictureFo.getWidth(), catchPictureFo.getHeight());
        } else {
            result = 0;
        }
        if (FfmpegUtil.CODE_SUCCESS.equals(result)) {
            OutputStream os = null;
            try (FileInputStream in = new FileInputStream(tempFile)) {
                //读取图片
                resp.setContentType("image/png");
                os = resp.getOutputStream();
                FfmPegCache.putFile(catchPictureFo, tempFile);
                IoUtil.copy(in, os);
            } catch (IOException e) {
                log.error("获取图片异常{}", e.getMessage());
                // 重置response
                resp.reset();
                resp.setContentType("application/json");
                resp.setCharacterEncoding("utf-8");
                throw new DataInconsistentException("获取图片异常");
            } finally {
                IoUtil.close(os);
            }
        } else {
            throw new IllegalArgumentException("图片截取失败");
        }
    }

}

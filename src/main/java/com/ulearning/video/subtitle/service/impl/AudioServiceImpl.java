package com.ulearning.video.subtitle.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.ulearning.video.common.utils.subtitle.AudioSubtitleFactory;
import com.ulearning.video.common.utils.subtitle.AudioSubtitleService;
import com.ulearning.video.common.utils.subtitle.eneity.Result;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleObject;
import com.ulearning.video.common.utils.upload.UploadFactory;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;
import com.ulearning.video.subtitle.dao.AudioSubtitleDao;
import com.ulearning.video.subtitle.fo.SubtitleFo;
import com.ulearning.video.subtitle.model.AudioSubtitleModel;
import com.ulearning.video.subtitle.service.AudioService;
import com.ulearning.video.subtitle.task.SubtitleTask;
import com.ulearning.video.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 音频服务实现类
 * @date 2022-09-21 11:54
 */
@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Autowired
    private AudioSubtitleDao audioSubtitleDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Integer subtitleTask(SubtitleFo subtitleFo) {
        if (StrUtil.isBlank(subtitleFo.getSource())) {
            log.error("源文件不能为空");
            return null;
        }

        // 创建任务记录
        AudioSubtitleModel creat = AudioSubtitleModel.creat(subtitleFo.getSource(), subtitleFo.getCallback());
        audioSubtitleDao.insert(creat);
        Integer id = creat.getId();
        if (Objects.nonNull(id)) {
            SubtitleTask.executor.execute(() -> {
                doSubtitleTask(creat);
            });
        }
        return id;
    }

    @Override
    public Integer videoToAudio(String videoPath, String audioPath) {
        return FfmpegUtil.videoToAudio(videoPath, audioPath);
    }

    @Override
    public String audioSubtitle(String audioPath, String callback) {
        // 创建语音转字幕任务, 获取任务id
        return AudioSubtitleFactory.getInstance().audioSubtitle(audioPath, callback);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSubtitle(AudioSubtitleModel task) {
        Result result = AudioSubtitleFactory.getInstance(task.getServiceName()).queryTask(task.getTaskId());
        // 转换成功
        if (Result.SUCCESS.equals(result.getCode())) {
            SubtitleObject subtitleObject = result.getSubtitleObject();
            subtitleObject.setTaskId(Long.valueOf(task.getId()));
            // 落库mongoDB
            mongoTemplate.insert(subtitleObject);
            // 更新任务执行状态
            audioSubtitleDao.updateStatus(task.getId(), task.getStatus(), AudioSubtitleModel.SUCCESS);
        }
        // 转换失败
        if (Result.ERROR.equals(result.getCode())) {
            // 更新任务执行状态
            audioSubtitleDao.updateStatus(task.getId(), task.getStatus(), AudioSubtitleModel.ERROR);
        }
    }

    private void doSubtitleTask(AudioSubtitleModel audioSubtitle) {
        Integer id = audioSubtitle.getId();
        String source = audioSubtitle.getSourceUrl();
        File mp3 = null;
        try {
            // 判断输入类型, 不是mp3格式需要先尝试转换成mp3
            if (!source.endsWith(".mp3")) {
                // 未开始 -> 格式转换中
                if (0 == audioSubtitleDao.updateStatus(id, AudioSubtitleModel.NOT_STARTED, AudioSubtitleModel.FORMAT_PROGRESS)) {
                    return;
                }
                // 创建临时mp3文件
                mp3 = File.createTempFile(IdUtil.fastSimpleUUID(), ".mp3");
                Integer result = FfmpegUtil.videoToAudio(source, mp3.getAbsolutePath());
                // 获取执行结果
                if (!FfmpegUtil.CODE_SUCCESS.equals(result)) {
                    audioSubtitle.setMsg("字幕生成失败, 视频生成mp3失败");
                    executionFailed(audioSubtitle.error());
                    log.error("------ 字幕生成失败, 视频生成mp3失败");
                    return;
                }
                source = mp3.getAbsolutePath();
                // 格式转换中 --> 生成字幕中
                audioSubtitleDao.updateStatus(id, AudioSubtitleModel.FORMAT_PROGRESS, AudioSubtitleModel.GENERATING_SUBTITLES);
            } else {
                // 未开始 --> 生成字幕中
                if (0 == audioSubtitleDao.updateStatus(id, AudioSubtitleModel.NOT_STARTED, AudioSubtitleModel.GENERATING_SUBTITLES)) {
                    return;
                }
            }
            String mp3Url;
            // 非网络资源,上传到oss
            if (!ReUtil.isMatch(StringUtil.URL_PATTERN, source)) {
                File file = new File(source);
                if (!file.exists()) {
                    audioSubtitle.setMsg("字幕生成失败, mp3路径不正确");
                    executionFailed(audioSubtitle.error());
                    log.error("------ 字幕生成失败, mp3路径不正确");
                    return;
                }
                mp3Url = UploadFactory.getInstance().uploadFile("resources/audio/subtitle/" + IdUtil.fastSimpleUUID() + "-" + file.getName(), file);
                if (StrUtil.isBlank(mp3Url)) {
                    audioSubtitle.setMsg("字幕生成失败, 文件上传失败");
                    executionFailed(audioSubtitle.error());
                    log.error("------ 字幕生成失败, 文件上传失败");
                    return;
                }
                // 添加音频资源信息
                AudioSubtitleModel taskInfo = AudioSubtitleModel.builder().id(id)
                        .audioUrl(mp3Url).build();
                audioSubtitleDao.update(taskInfo);
            } else {
                mp3Url = source;
            }
            // 获取服务实例
            AudioSubtitleService service = AudioSubtitleFactory.getInstance();
            // 服务名
            String serverName = service.serverName();
            // 创建字幕生成任务
            String taskId = service.audioSubtitle(mp3Url, audioSubtitle.getCallback());
            if (StrUtil.isBlank(taskId)) {
                audioSubtitle.setMsg("字幕生成失败, 创建字幕转换任务失败");
                executionFailed(audioSubtitle.error());
                log.error("------ 字幕生成失败, 创建字幕转换任务失败");
                return;
            } else {
                // 修改日志信息 增加服务名和和任务id
                AudioSubtitleModel taskInfo = AudioSubtitleModel.builder().id(id)
                        .serviceName(serverName)
                        .taskId(taskId).build();
                audioSubtitleDao.update(taskInfo);
            }
        } catch (Exception e) {
            log.error("------ 字幕生成失败, 任务执行异常", e);
            audioSubtitle.setMsg("字幕生成失败, 任务执行异常" + e.getMessage());
            executionFailed(audioSubtitle.error());
        } finally {
            if (Objects.nonNull(mp3)) {
                mp3.delete();
            }
        }
    }

    private void executionFailed(AudioSubtitleModel audioSubtitleModel) {
        // 异常回调
        if (StrUtil.isNotBlank(audioSubtitleModel.getCallback())) {
            // TODO
            log.info("执行异常回调");
        }
        audioSubtitleDao.update(audioSubtitleModel);
    }
}

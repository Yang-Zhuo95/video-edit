package com.ulearning.video.ffmpeg.actuator;

import com.alibaba.fastjson.JSON;
import com.ulearning.video.editor.fo.MultipleMergeFo;
import com.ulearning.video.ffmpeg.executor.FfmPegExecutor;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;
import lombok.EqualsAndHashCode;

import java.io.File;

/**
 * @author yangzhuo
 * @description 视频合成(多宫格拼接)执行器
 * @date 2022-08-02 14:07
 */
@EqualsAndHashCode
public class MultipleMergeActuator extends BaseActuator {

    private MultipleMergeFo multipleMergeFo;

    public MultipleMergeActuator(MultipleMergeFo multipleMergeFo) {
        this.multipleMergeFo = multipleMergeFo;
    }

    @Override
    public String createCmd() {
        return FfmpegUtil.multipleMerge(
                multipleMergeFo.getVideoInfos(), multipleMergeFo.getSubtitlePath(),
                multipleMergeFo.getBaseWidth(), multipleMergeFo.getBaseHeight(),
                multipleMergeFo.getAudioIndex(), FfmPegExecutor.getPath()
        );
    }

    @Override
    public Integer execute() {
        throw new IllegalArgumentException("该任务不支持直接执行");
    }

    @Override
    public String getOutputPath() {
        return multipleMergeFo.getOutputPath();
    }

    @Override
    public void setOutputPath(String outputPath) {
        multipleMergeFo.setOutputPath(outputPath);
    }

    @Override
    public File getOutputFile() {
        return new File(multipleMergeFo.getOutputPath());
    }

    @Override
    public void setOutputFile(File outputFile) {
        multipleMergeFo.setOutputPath(outputFile.getAbsolutePath());
    }

    @Override
    public Object unWarp() {
        return this.multipleMergeFo;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(multipleMergeFo);
    }
}

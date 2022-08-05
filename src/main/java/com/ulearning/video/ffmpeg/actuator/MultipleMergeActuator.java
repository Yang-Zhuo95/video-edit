package com.ulearning.video.ffmpeg.actuator;

import com.alibaba.fastjson.JSON;
import com.ulearning.video.editor.fo.MultipleMergeFo;
import com.ulearning.video.ffmpeg.executor.FfmPegExecutor;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;

/**
 * @author yangzhuo
 * @description 视频合成(多宫格拼接)执行器
 * @date 2022-08-02 14:07
 */
public class MultipleMergeActuator extends BaseActuator {

    private MultipleMergeFo multipleMergeFo;

    public MultipleMergeActuator(MultipleMergeFo multipleMergeFo) {
        this.multipleMergeFo = multipleMergeFo;
    }

    @Override
    public String createCmd() {
        return FfmpegUtil.multipleMerge(multipleMergeFo.getVideoInfos(),
                multipleMergeFo.getBaseWidth(), multipleMergeFo.getBaseHeight(),
                multipleMergeFo.getAudioIndex(), FfmPegExecutor.getPath()
        );
    }

    @Override
    public String getOutputPath() {
        return multipleMergeFo.getOutputPath();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(multipleMergeFo);
    }
}

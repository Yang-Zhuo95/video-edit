package com.ulearning.video.ffmpeg.actuator;


import cn.hutool.core.util.IdUtil;
import com.ulearning.video.editor.fo.CatchPictureFo;
import com.ulearning.video.ffmpeg.cache.FfmPegCache;
import com.ulearning.video.ffmpeg.config.FfmPegConfig;
import com.ulearning.video.ffmpeg.util.FfmpegUtil;

import java.io.File;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 视频截帧执行器
 * @date 2022-08-08 13:16
 */
public class CatchPictureActuator extends BaseActuator {

    private CatchPictureFo catchPictureFo;

    private File file;

    public CatchPictureActuator(CatchPictureFo catchPictureFo) {
        this.catchPictureFo = catchPictureFo;
    }

    @Override
    public String createCmd() {
        throw new IllegalArgumentException("该任务暂不支持生成命令行");
    }

    @Override
    public Integer execute() {
        if (FfmPegCache.containsFileKey(catchPictureFo)) {
            this.file = FfmPegCache.getFile(catchPictureFo);
            return FfmpegUtil.CODE_SUCCESS;
        }
        file = new File(FfmPegConfig.WORK_SPACE + System.currentTimeMillis() + "-" + IdUtil.simpleUUID() + ".png");
        file.deleteOnExit();
        Integer result = FfmpegUtil.catchJpg(catchPictureFo.getSource(), file.getAbsolutePath(),
                catchPictureFo.getDuration().toString(), catchPictureFo.getWidth(), catchPictureFo.getHeight());
        if (file.exists()) {
            FfmPegCache.putFile(catchPictureFo, file);
            return result;
        }
        return result;
    }

    @Override
    public String getOutputPath() {
        return file.getAbsolutePath();
    }

    @Override
    public void setOutputPath(String outputPath) {
        file = new File(outputPath);
    }

    @Override
    public File getOutputFile() {
        return this.file;
    }

    @Override
    public void setOutputFile(File outputFile) {
        this.file = outputFile;
    }

    @Override
    public Object unWarp() {
        return this.catchPictureFo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatchPictureActuator that = (CatchPictureActuator) o;
        return Objects.equals(catchPictureFo, that.catchPictureFo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchPictureFo);
    }
}

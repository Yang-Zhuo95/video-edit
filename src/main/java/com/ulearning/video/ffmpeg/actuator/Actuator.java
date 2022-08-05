package com.ulearning.video.ffmpeg.actuator;

/**
 * @author yangzhuo
 * @description 执行器接口
 * @date 2022-08-02 14:10
 */
public interface Actuator {
    /**
     * 生成cmd命令
     * @return String cmd命令
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    String createCmd();
    /**
     * 执行任务
     * @return 0-成功 1-失败
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    Integer execute();
    /**
     * 获取输出路径
     * @return String 输出路径
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    String getOutputPath();
}

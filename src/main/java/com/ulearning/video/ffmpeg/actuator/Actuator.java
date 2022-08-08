package com.ulearning.video.ffmpeg.actuator;

import java.io.File;

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

    /**
     * 设置输出路径
     * @param outputPath 输出路径
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    void setOutputPath(String outputPath);

    /**
     * 获取输出文件
     * @return File 输出文件
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    File getOutputFile();

    /**
     * 设置输出文件
     * @param outputFile 输出文件
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    void setOutputFile(File outputFile);

    /**
     * 返回被执行器包装的表单对象
     * @return Object 表单对象
     * @date 2022/8/2 14:08
     * @author yangzhuo
     */
    Object unWarp();
}

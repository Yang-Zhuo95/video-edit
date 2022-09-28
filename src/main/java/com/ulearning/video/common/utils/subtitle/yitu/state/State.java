package com.ulearning.video.common.utils.subtitle.yitu.state;

/**
 * @author yangzhuo
 * @description 状态码
 * @date 2022-09-23 16:22
 */
public class State {
    //  转写任务状态
    // 0 - LOADING - 文件加载中
    // 1 - QUEUEING - 排队中
    // 2 - PROGRESSING - 正在转写
    // 3 - TASK_SUCC - 转写完成

    /**
     * 请求处理结果的错误代码。 0表示OK，否则表示异常
     */
    public static final Integer RTN_SUCCESS = 0;

    /**
     * 文件加载中
     */
    public static final Integer LOADING = 0;

    /**
     * 排队中
     */
    public static final Integer QUEUEING = 1;

    /**
     * 正在转写
     */
    public static final Integer PROGRESSING = 2;

    /**
     * 转写完成
     */
    public static final Integer TASK_SUCC = 3;

    /**
     * 文件加载失败
     */
    public static final Integer TFILE_LOADING_FAILD = 6;
}

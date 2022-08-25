package com.ulearning.video.common.utils;

import com.ulearning.video.common.enums.ReturnStatus;
import lombok.Data;

/**
 * 接口统一返回结果
 * <p>
 * 推荐使用
 * <p>成功：R.success() </p>
 * <p>成功：R.success(data) </p>
 * <p>成功：R.success(data,msg)</p>
 * <p>出错：R.error()</p>
 * <p>出错：R.error(msg)</p>
 * <p>验证型错误：R.warn(msg)</p>
 * <p>定义参数的错误：R.create(returnStatus)</p>
 * @param <T>
 */
@Data
public class R<T> {
    public int code;
    public T data;
    public String msg;

    /**
     * 私有构建方法，请使用静态方法构建实例
     */
    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 参数验证失败
     * @param msg
     * @return
     */
    public static <T> R<T> warn(String msg) {
        return new R<T>(ReturnStatus.ValidateFailure.getValue(), msg, null);
    }

    /**
     * 创建实例
     * @return
     */
    public static <T> R<T> create(ReturnStatus returnStatus) {
        return new R<T>(returnStatus.getValue(), returnStatus.getName(), null);
    }

    /**
     * 创建实例
     * @return
     */
    public static <T> R<T> create(ReturnStatus returnStatus, String msg) {
        return new R<T>(returnStatus.getValue(), msg, null);
    }

    /**
     * 出错
     * @param msg
     * @return
     */
    public static <T> R<T> error(String msg) {
        return new R<T>(ReturnStatus.Error.getValue(), msg, null);
    }

    /**
     * 出错
     * @return
     */
    public static <T> R<T> error() {
        return new R<T>(ReturnStatus.Error.getValue(), ReturnStatus.Error.getName(), null);
    }

    /**
     * 成功
     * @param <T>
     * @return
     */
    public static <T> R<T> success() {
        return new R<T>(ReturnStatus.Success.getValue(), ReturnStatus.Success.getName(), null);
    }

    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> success(T data) {
        return new R<T>(ReturnStatus.Success.getValue(), "", data);
    }

    /**
     * 成功
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> success(String msg) {
        return new R<T>(ReturnStatus.Success.getValue(), msg, null);
    }

    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> success(T data, String msg) {
        return new R<T>(ReturnStatus.Success.getValue(), msg, data);
    }

    /**
     * 失败
     * @param <T>
     * @return
     */
    public static <T> R<T> fail() {
        return new R<T>(ReturnStatus.OPERAFAIL.getValue(), ReturnStatus.OPERAFAIL.getName(), null);
    }

}

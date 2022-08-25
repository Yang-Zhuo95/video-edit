package com.ulearning.video.common.utils;

import com.ulearning.video.common.exception.DataInconsistentException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 文件工具类
 * @date 2022-08-11 11:17
 */
@Slf4j
public class ResponseUtil {

    private ResponseUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 向http响应写文件
     * @param file 文件
     * @param resp 响应体
     * @date 2022/8/11 11:18
     * @author yangzhuo
     */
    public static void writeFileToResponse(File file, String contentType,String charset, HttpServletResponse resp) throws IOException {
        FileChannel sourceChannel = null;
        WritableByteChannel respChannel = null;
        try (RandomAccessFile sourceFile = new RandomAccessFile(file, "r")) {
            //读取图片
            resp.setContentType(contentType);
            sourceChannel = sourceFile.getChannel();
            respChannel = Channels.newChannel(resp.getOutputStream());
            // 一般图片大小不会超过2.5GB
            sourceChannel.transferTo(sourceChannel.position(), sourceChannel.size(), respChannel);
        } catch (IOException e) {
            String msg;
            if (e instanceof FileNotFoundException) {
                msg = "生成图片异常";
            } else {
                msg = "获取图片异常";
            }
            log.error(msg + "{}", e.getMessage());
            // 重置response
            resp.reset();
            resp.setContentType("application/json");
            resp.setCharacterEncoding(charset);
            throw new DataInconsistentException(msg);
        } finally {
            if (Objects.nonNull(respChannel)) {
                respChannel.close();
            }
            if (Objects.nonNull(sourceChannel)) {
                sourceChannel.close();
            }
        }
    }
}

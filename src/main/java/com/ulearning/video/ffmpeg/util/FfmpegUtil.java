package com.ulearning.video.ffmpeg.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import com.ulearning.video.common.exception.DataInconsistentException;
import com.ulearning.video.ffmpeg.cache.FfmPegCache;
import com.ulearning.video.ffmpeg.executor.FfmpegCmd;
import com.ulearning.video.ffmpeg.dao.VideoEditRecordDao;
import com.ulearning.video.ffmpeg.entity.Period;
import com.ulearning.video.ffmpeg.entity.VideoInfo;
import com.ulearning.video.ffmpeg.enums.VideoSuffixEnum;
import com.ulearning.video.ffmpeg.executor.FfmPegExecutor;
import com.ulearning.video.ffmpeg.model.VideoEditRecordModel;
import com.ulearning.video.ffmpeg.task.TaskWithResult;
import com.ulearning.video.utils.ApplicationUtils;
import com.ulearning.video.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.MultimediaInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yangzhuo
 * @description 基于ffmpeg的音视频编辑工具
 * @date 2021/11/11 9:08
 */
@Slf4j
public class FfmpegUtil {

    //执行成功0,失败1
    public static Integer CODE_SUCCESS = 0;
    public static Integer CODE_FAIL = 1;
    //默认静音分贝
    public static Integer DEFAULT_DECIBEL = 50;
    //起止时间正则校验
    public static String TIME_CHECK_01 = "([0-5][0-9]):([0-5][0-9]):([0-5][0-9])+(|\\.[0-9]{3})$";
    public static String TIME_CHECK_02 = "^[0-9]+([.]{1}[0-9]+){0,1}$";

    /**
     * 直接复制
     */
    public static String COPY = "-c copy";

    /**
     * 设置尺寸
     */
    public static String RESIZE = "-s %sx%s";

    /**
     * 执行线程数
     */
    public static String THREADS = "-threads %s -preset ultrafast ";

    /**
     * 将视频转为帧内编码
     */
    // private static String intra_frame_coding = " -y  -i %s -sameq -intra %s";

    /**
     * 抓取视频特定时间图片
     */
    public static String catch_jpg = "  -y -ss %s -i %s %s -q:v 1 -vframes 1 %s";

    /**
     * 剪辑视频
     */
    public static String edit_video = " -y -ss %s -to %s -i %s %s %s %s";

    /**
     * 过滤静默音频
     */
    public static String silence_period = " -y -i %s -af silencedetect=n=-%sdB:d=%s -f null -";

    /**
     * 合成视频
     */
    public static String composite_video = " -y -f concat -safe 0 -i %s %s %s %s";

    /**
     * 编码格式转换
     */
    public static String format = " -y -i %s -f %s %s %s";

    /**
     * 添加水印
     */
    public static String cmd_mov_water = " -y -i %s -vf \"drawtext=fontfile=Arial.ttf:text='441606747@qq.com':y=h-line_h-20:x=(w-text_w)/2:fontsize=34:fontcolor=yellow:shadowy=2\" -b:v 3000k %s";

    /**
     * 获取视频的关键帧
     */
    public static String find_crux_frame = " -y -i %s -vf select='eq(pict_type\\,I)' -vsync 2 -f image2 %s";

    /**
     * 根据关键帧裁剪视频
     */
    public static String cut_by_crux_frame = " -y -i %s -acodec copy -f segment -vcodec copy -reset_timestamps 1 -map 0 %s-%d.mp4";

    /**
     * 每隔x个关键帧抽一张图片
     */
    // ffmpeg -i input.flv -vf "select='eq(pict_type,PICT_TYPE_I)'" -vsync vfr image_%d.png

    /**
     * <p>执行ffmpeg自定义命令</p>
     * @param cmdStr 自定义命令
     * @return 0-成功
     */
    public static Integer cmdExecute(String cmdStr) {
        //code=0表示正常
        Integer code = 1;
        FfmpegCmd ffmpegCmd = new FfmpegCmd();
        BufferedReader inBr = null;
        try {
            //destroyOnRuntimeShutdown表示是否立即关闭Runtime
            //如果ffmpeg命令需要长时间执行，destroyOnRuntimeShutdown = false

            //openIOStreams表示是不是需要打开输入输出流:
            //	       inputStream = processWrapper.getInputStream();
            //	       outputStream = processWrapper.getOutputStream();
            //	       errorStream = processWrapper.getErrorStream();
            ffmpegCmd.execute(false, true, cmdStr);
            Integer taskId = FfmPegExecutor.getTaskId();
            // BufferedInputStream in = new BufferedInputStream();
            inBr = new BufferedReader(new InputStreamReader(ffmpegCmd.getErrorStream()));
            String lineStr;
            String lastStr = null;
            boolean hasInfo = false;
            while ((lineStr = inBr.readLine()) != null) {
                int nTimeStartIndex = lineStr.indexOf("time=");
                int nTimeEndIndex = lineStr.indexOf(" bitrate=");
                if (nTimeStartIndex > 0 && nTimeEndIndex > 0) {
                    hasInfo = true;
                    String duration = lineStr.substring(nTimeStartIndex + 5, nTimeEndIndex);
                    if (Objects.nonNull(taskId)) {
                        FfmPegCache.updateProgress(taskId, duration);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(duration);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(lineStr);
                    }
                    lastStr = lineStr;
                }
            }
            if (Objects.nonNull(lastStr) && !hasInfo) {
                log.error(lastStr);
            }
            //code=0表示正常
            code = ffmpegCmd.getProcessExitCode();
            FfmPegExecutor.setMsg(code == 0 ? "success" : lastStr);
        } catch (IOException e) {
            FfmPegExecutor.setMsg(e.getMessage());
            log.error("{} |taskId: {}", e.getMessage(), FfmPegExecutor.getTaskId());
        } finally {
            //关闭资源
            ffmpegCmd.close();
            if (Objects.nonNull(inBr)) {
                try {
                    inBr.close();
                } catch (IOException e) {
                    log.warn("Error closing input stream", e);
                }
            }
        }
        //返回
        return code;
    }

    /**
     * <p>执行ffmpeg自定义过滤器命令</p>
     * @param cmdStr 自定义过滤器命令
     * @return 0-成功
     */
    public static List<Period> cmdAudioFilter(String cmdStr) {
        //code=0表示正常
        List<Period> silencePeriodList = new ArrayList<>();
        FfmpegCmd ffmpegCmd = new FfmpegCmd();
        // 错误流
        InputStream errorStream = null;
        try {
            //destroyOnRuntimeShutdown表示是否立即关闭Runtime
            //如果ffmpeg命令需要长时间执行，destroyOnRuntimeShutdown = false

            //openIOStreams表示是不是需要打开输入输出流:
            //	       inputStream = processWrapper.getInputStream();
            //	       outputStream = processWrapper.getOutputStream();
            //	       errorStream = processWrapper.getErrorStream();
            ffmpegCmd.execute(false, true, cmdStr);
            errorStream = ffmpegCmd.getErrorStream();

            StringBuffer stringBuffer = new StringBuffer();
            // 打印过程
            int len = 0;
            while ((len = errorStream.read()) != -1) {
                stringBuffer.append((char) len);
            }
            int indexStart = 0;
            int indexEnd;
            while (true) {
                indexStart = stringBuffer.indexOf("silence_end:", indexStart + 1);
                indexEnd = stringBuffer.indexOf(" ", indexStart);
                if (indexStart < 0 || indexEnd < 0) {
                    break;
                }
                String str = stringBuffer.substring(indexStart, indexEnd);
                String endStr = str.substring("silence_end: ".length(), str.indexOf(" | "));
                String durationStr = str.substring(str.indexOf(" | ") + " | silence_duration: ".length());
                BigDecimal end = BigDecimal.valueOf(Double.parseDouble(endStr));
                BigDecimal duration = BigDecimal.valueOf(Double.parseDouble(durationStr));
                BigDecimal start = end.subtract(duration);
                Period silencePeriod = Period.builder()
                        .start((start.compareTo(BigDecimal.ZERO) < 0) ? BigDecimal.ZERO : start)
                        .end(end)
                        .duration(duration)
                        .build();
                silencePeriodList.add(silencePeriod);
            }
            //code=0表示正常
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            ffmpegCmd.close();
        }
        //返回
        return silencePeriodList;
    }

    /**
     * 编码格式转换
     * @param source  源视频文件
     * @param target  目标视频文件
     * @param threads 执行线程数,不使用多线程传null
     * @return 0-成功 1-失败
     */
    public static Integer format(String source, String target, Integer threads) {
        // 参数校验
        checkVideoFilePath(source, target);
        String thread = "";
        if (Objects.nonNull(threads) && threads > 0) {
            thread = String.format(THREADS, threads);
        }
        return cmdExecute(String.format(format, source, target.substring(target.lastIndexOf(".") + 1), thread, target));
    }

    /**
     * 视频转音频
     * @param videoPath 源视频文件
     * @param audioPath 目标音频文件
     * @return 0-成功 1-失败
     */
    public static Integer videoToAudio(String videoPath, String audioPath) {
        checkVideoFilePath(videoPath);
        checkFileSource(audioPath);
        File fileMp4 = new File(videoPath);
        File fileMp3 = new File(audioPath);

        //Audio Attributes
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(128000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        //Encoding attributes 编码属性
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        MultimediaObject mediaObject = new MultimediaObject(fileMp4);
        try {
            encoder.encode(mediaObject, fileMp3, attrs);
            log.info("File MP4 convertito in MP3");
            return CODE_SUCCESS;
        } catch (Exception e) {
            log.error("File non convertito");
            log.error(e.getMessage());
            return CODE_FAIL;
        }
    }

    /**
     * 获取视频基本信息
     * @param fileSource 源视频
     * @return VideoInfo 视频基本信息
     */
    public static VideoInfo getVideoInfo(String fileSource) {
        checkFileSource(fileSource);
        if (ReUtil.isMatch(StringUtil.URL_PATTERN, fileSource)) {
            try {
                return getVideoInfo(new URL(fileSource));
            } catch (MalformedURLException e) {
                FfmPegExecutor.setMsg(e.getMessage());
                log.error("{} |taskId: {}", e.getMessage(), FfmPegExecutor.getTaskId());
                return null;
            }
        } else {
            return getVideoInfo(new File(fileSource));
        }
    }

    public static VideoInfo getVideoInfo(URL source) {
        VideoInfo videoInfo = null;
        try {
            MultimediaObject multimediaObject = new MultimediaObject(source);
            MultimediaInfo m = multimediaObject.getInfo();
            videoInfo = VideoInfo.builder()
                    .source(source.toString())
                    .width(m.getVideo().getSize().getWidth())
                    .height(m.getVideo().getSize().getHeight())
                    .duration(m.getDuration())
                    .format(m.getFormat())
                    .build();
        } catch (Exception e) {
            FfmPegExecutor.setMsg(e.getMessage());
            log.error("{} |taskId: {}", e.getMessage(), FfmPegExecutor.getTaskId());
        }
        return videoInfo;
    }

    public static VideoInfo getVideoInfo(File source) {
        FileInputStream fis = null;
        FileChannel fc = null;
        VideoInfo videoInfo = null;
        try {
            MultimediaObject multimediaObject = new MultimediaObject(source);
            MultimediaInfo m = multimediaObject.getInfo();
            fis = new FileInputStream(source);
            fc = fis.getChannel();
            videoInfo = VideoInfo.builder()
                    .source(source.getAbsolutePath())
                    .width(m.getVideo().getSize().getWidth())
                    .height(m.getVideo().getSize().getHeight())
                    .duration(m.getDuration())
                    .format(m.getFormat())
                    .build();
        } catch (Exception e) {
            FfmPegExecutor.setMsg(e.getMessage());
            log.error("{} |taskId: {}", e.getMessage(), FfmPegExecutor.getTaskId());
        } finally {
            if (null != fc) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return videoInfo;
    }

    /**
     * 抓取视频特定时间图片
     * @param source 源视频文件
     * @param target 目标jpg文件
     * @param time   抓取的时间
     * @param width  宽度
     * @param height 高度
     * @return 0-成功 1-失败
     */
    public static Integer catchPicture(String source, String target, String time, Integer width, Integer height) {
        checkVideoFilePath(source);
        checkFileSource(target);
        checkTime(time);
        String reSize = "";
        if (Objects.nonNull(width) && Objects.nonNull(height)) {
            reSize = String.format(RESIZE, width, height);
        }
        return cmdExecute(String.format(catch_jpg, time, source, reSize, target));
    }

    /**
     * 剪辑视频
     * @param source    源文件绝对地址 C:\test\source.mp4
     * @param target    目标文件绝对地址 C:\test\target.mp4
     * @param startTime 剪辑起始时间 00:00:00.000
     * @param endTime   剪辑终止时间 00:00:00.000
     * @param encode    是否重编码 true-重编码 false-不重编码(直接复制)
     * @param threads   执行线程数,不使用多线程传null
     * @return 0-成功 1-失败
     */
    public static Integer editVideo(String source, String target, String startTime, String endTime, boolean encode, Integer threads) {
        // 参数校验
        checkVideoFilePath(source, target);
        checkTime(startTime, endTime);
        String copy = "";
        if (!encode) {
            copy = COPY;
        }
        String thread = "";
        if (Objects.nonNull(threads) && threads > 0) {
            thread = String.format(THREADS, threads);
        }
        return cmdExecute(String.format(edit_video, startTime, endTime, source, copy, thread, target));
    }

    /**
     * 视频合成
     * @param filePaths 视频绝对路径集合 C:\test\source1.mp4, C:\test\source2.mp4, C:\test\source3.mp4
     * @param target    目标文件绝对地址 C:\test\target.mp4
     * @param encode    是否重编码 true-重编码 false-不重编码(直接复制)
     * @param threads   执行线程数,不使用多线程传null
     * @return 0-成功 1-失败
     */
    public static Integer compositeVideo(List<String> filePaths, String target, boolean encode, Integer threads) {
        // 参数校验
        checkVideoFilePath(target);
        if (CollectionUtil.isNotEmpty(filePaths)) {
            filePaths.parallelStream().forEach(FfmpegUtil::checkVideoFilePath);
        } else {
            log.error("视频路径集合不能为空");
            return CODE_FAIL;
        }

        int size = filePaths.size();
        String[] fileNameLineArr = new String[size];
        String[] mpegFilePathArr = new String[size];
        List<Future<Integer>> futures = new Vector<>();
        ExecutorService executor = ExecutorBuilder.create()
                .setCorePoolSize(Runtime.getRuntime().availableProcessors())
                .setMaxPoolSize(Runtime.getRuntime().availableProcessors())
                .setWorkQueue(new LinkedBlockingQueue<>(size))
                .build();
        for (int i = 0; i < size; i++) {
            Future<Integer> future = executor.submit(new TaskWithResult(i) {
                @Override
                public Integer call() throws Exception {
                    int id = this.getId();
                    String filePath = filePaths.get(id);
                    if (VideoSuffixEnum.mpeg.name().equals(filePath.substring(filePath.lastIndexOf(".") + 1))) {
                        fileNameLineArr[id] = String.format("file '%s'", filePath);
                        return CODE_SUCCESS;
                    }
                    String mpegFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + VideoSuffixEnum.mpeg.getSuffix();
                    Integer format = format(filePath, mpegFilePath, threads);
                    if (CODE_SUCCESS.equals(format)) {
                        fileNameLineArr[id] = String.format("file '%s'", mpegFilePath);
                        mpegFilePathArr[id] = mpegFilePath;
                    }
                    return CODE_SUCCESS;
                }
            });
            futures.add(future);
        }

        try {
            for (Future<Integer> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("视频转码失败");
            List<String> mpegFilePaths = Arrays.asList(mpegFilePathArr);
            // 删除失败的文件
            if (CollectionUtil.isNotEmpty(mpegFilePaths)) {
                mpegFilePaths.parallelStream().forEach(f -> {
                    if (Objects.nonNull(f)) {
                        new File(f).delete();
                    }
                });
            }
            return CODE_FAIL;
        } finally {
            executor.shutdownNow();
        }
        List<String> fileNameLines = Arrays.asList(fileNameLineArr);
        List<String> mpegFilePaths = Arrays.asList(mpegFilePathArr);
        try {
            mpegFilePaths.parallelStream().forEach(FfmpegUtil::checkVideoFilePath);
        } catch (DataInconsistentException e) {
            log.error("视频转码失败");
            // 删除失败的文件
            if (CollectionUtil.isNotEmpty(mpegFilePaths)) {
                mpegFilePaths.parallelStream().forEach(f -> {
                    if (Objects.nonNull(f)) {
                        new File(f).delete();
                    }
                });
            }
            return CODE_FAIL;
        }

        String path = target.substring(0, target.lastIndexOf(File.separator) + 1);
        String fileList = System.currentTimeMillis() + "-" + IdUtil.fastUUID();
        String filePath = path + fileList;
        FileWriter writer = new FileWriter(filePath);
        writer.writeLines(fileNameLines);
        String copy = "";
        if (!encode) {
            copy = COPY;
        }
        String thread = "";
        if (Objects.nonNull(threads) && threads > 0) {
            thread = String.format(THREADS, threads);
        }
        Integer result = CODE_FAIL;
        String mpegTarget = target.substring(0, target.lastIndexOf(".")) + VideoSuffixEnum.mpeg.getSuffix();
        Integer execut = cmdExecute(String.format(composite_video, filePath, copy, thread, mpegTarget));
        mpegFilePaths.parallelStream().forEach(f -> {
            new File(f).delete();
        });
        if (CODE_SUCCESS.equals(execut)) {
            if (target.substring(target.lastIndexOf(File.separator) + 1).equals(VideoSuffixEnum.mpeg.name())) {
                new File(filePath).delete();
                return execut;
            }
            result = format(mpegTarget, target, threads);
        }
        new File(mpegTarget).delete();
        if (CODE_FAIL.equals(result)) {
            new File(target).delete();
        }
        new File(filePath).delete();
        return result;
    }

    /**
     * 静默片段剪辑
     * 将视频中的静音片段自动剪辑掉
     * @param source    源视频绝对路径
     * @param target    目标视频绝对路径
     * @param duration  静默时长
     * @param precision 剪辑精度(剪掉多少秒内的杂音)
     * @param decibel   分贝数建议值:30-40 小于0或为null时默认50
     * @param threads   执行线程数,不使用多线程传null
     * @return 0-成功 1-失败
     */
    public static String silenceEdit(String source, String target, Double duration, Double precision, Integer decibel, Integer threads) {
        // 校验文件格式
        try {
            checkVideoFilePath(source, target);
        } catch (DataInconsistentException e) {
            log.error("source:{} {} 视频格式不正确,不支持该格式的视频文件", source, target);
            return source;
        }
        if (Objects.isNull(duration) || duration <= 0) {
            log.error("静默时间设置错误,时间必须大于0");
            return source;
        }
        if (Objects.isNull(precision) || duration < 0) {
            log.error("静默时间精度设置错误,时间不能小于0");
            return source;
        }
        // 监听静默片段
        List<Period> silencePeriods = getSilencePeriods(source, duration, decibel);
        // 获取源视频基本信息
        VideoInfo videoInfo = getVideoInfo(source);
        // 获取视频时长
        double d = videoInfo.getDuration().doubleValue() / 1000;
        // 获取有声片段
        List<Period> audioPeriods = getAudioPeriods(silencePeriods, d, precision);
        // 裁剪合成有声片段
        Integer result = silenceEdit(source, target, audioPeriods, d, threads);
        // 成功返回目标视频路径,失败返回源视频路径
        return Objects.equals(CODE_SUCCESS, result) ? target : source;
    }

    /**
     * 静默片段剪辑
     * 将视频中的静音片段自动剪辑掉
     * @param source       源视频绝对路径
     * @param target       目标视频绝对路径
     * @param audioPeriods 有声时间片段
     * @param duration     源视频时长
     * @param threads      执行线程数,不使用多线程传null
     * @return 0-成功 1-失败
     */
    public static Integer silenceEdit(String source, String target, List<Period> audioPeriods, Double duration, Integer threads) {
        // 文件格式校验
        checkVideoFilePath(source, target);
        // 判断有声片段
        if (CollectionUtils.isEmpty(audioPeriods)) {
            log.error("有声片段不能为空");
            return CODE_FAIL;
        }
        // 如果无视频时长参数,去查询
        if (Objects.isNull(duration)) {
            VideoInfo videoInfo = getVideoInfo(source);
            duration = videoInfo.getDuration().doubleValue() / 1000;
        }
        // 获取目标路径
        String path = target.substring(0, target.lastIndexOf(File.separator) + 1);
        // 获取文件后缀
        String suffixWithPoint = source.substring(source.lastIndexOf("."));
        int size = audioPeriods.size();
        if (size > 1) { //有声片段大于1
            String[] filePaths = new String[size];
            List<Future<Integer>> futures = new Vector<>();
            ExecutorService executor = ExecutorBuilder.create()
                    .setCorePoolSize(Runtime.getRuntime().availableProcessors())
                    .setMaxPoolSize(Runtime.getRuntime().availableProcessors())
                    .setWorkQueue(new LinkedBlockingQueue<>(size))
                    .build();
            for (int i = 0; i < size; i++) {
                Future<Integer> future = executor.submit(new TaskWithResult(i) {
                    @Override
                    public Integer call() {
                        int id = this.getId();
                        String filePath = path + id + "-" + IdUtil.fastUUID() + suffixWithPoint;
                        filePaths[id] = filePath;
                        Period period = audioPeriods.get(id);
                        // 视频裁剪
                        return editVideo(source, filePath, period.getStart().toString(), period.getEnd().toString(), true, threads);
                    }
                });
                futures.add(future);
            }
            try {
                for (Future<Integer> future : futures) {
                    future.get();
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("视频剪辑失败");
                List<String> filePathList = Arrays.asList(filePaths);
                filePathList.parallelStream().forEach(f -> {
                    if (Objects.nonNull(f)) {
                        new File(f).delete();
                    }
                });
                return CODE_FAIL;
            } finally {
                executor.shutdownNow();
            }
            List<String> filePathList = Arrays.asList(filePaths);
            // 视频合成
            Integer result = compositeVideo(filePathList, target, true, threads);
            filePathList.parallelStream().forEach(f -> {
                if (Objects.nonNull(f)) {
                    new File(f).delete();
                }
            });
            return result;
        } else {
            Period period = audioPeriods.get(0);
            if (period.getDuration().equals(BigDecimal.valueOf(duration))) {
                log.error("有声片段与源视频时长一致,无需转换");
                return CODE_FAIL;
            }
            // 视频裁剪
            return editVideo(source, target, period.getStart().toString(), period.getEnd().toString(), true, threads);
        }
    }

    /**
     * 获取静默时间片段集合
     * @param source   源视频/音频文件
     * @param duration 允许最大静默时长
     * @param decibel  分贝数建议值:30-40 小于0或为null时默认50
     * @return 静默时间片段集合
     */
    public static List<Period> getSilencePeriods(String source, double duration, Integer decibel) {
        checkFileSource(source);
        if (Objects.isNull(decibel) || decibel < 0) {
            decibel = DEFAULT_DECIBEL;
        }
        return cmdAudioFilter(String.format(" -y -i %s -af silencedetect=n=-%sdB:d=%s -f null -", source, decibel, duration));
    }

    /**
     * 获取有声时间片段集合
     * @param silencePeriods 静默时间片段集合
     * @param duration       视频/音频总时长
     * @param precision      允许的间隔精度
     * @return 有声时间片段集合
     */
    public static List<Period> getAudioPeriods(List<Period> silencePeriods, double duration, double precision) {
        List<Period> periods = new ArrayList<>();
        //静默片段为空时
        if (CollectionUtils.isEmpty(silencePeriods)) {
            BigDecimal end = new BigDecimal(duration);
            Period period = Period.builder()
                    .start(BigDecimal.ZERO)
                    .end(end)
                    .duration(end)
                    .build();
            periods.add(period);
            return periods;
        }
        Period.PeriodBuilder builder = Period.builder();
        BigDecimal bPrecision = new BigDecimal(precision);
        BigDecimal bDuration = new BigDecimal(duration);
        int size = silencePeriods.size();
        Period start = silencePeriods.get(0);
        // 首有声片段截取
        if (start.getStart().subtract(BigDecimal.ZERO).compareTo(bPrecision) > 0) {
            Period period = builder.start(BigDecimal.ZERO)
                    .end(start.getStart())
                    .duration(start.getStart()).build();
            periods.add(period);
        }
        // 中间有声片段截取
        for (int i = 1; i < size; i++) { // 去掉首尾遍历
            Period before = silencePeriods.get(i - 1);
            Period current = silencePeriods.get(i);
            // 判断当前开头和前一个的结尾
            if (current.getStart().subtract(before.getEnd()).compareTo(bPrecision) > 0) {
                Period period = builder.start(before.getEnd())
                        .end(current.getStart())
                        .duration(current.getStart().subtract(before.getEnd())).build();
                periods.add(period);
            }
        }
        // 尾有声片段截取
        Period end = silencePeriods.get(size - 1);
        if (bDuration.subtract(end.getEnd()).compareTo(bPrecision) > 0) {
            Period period = builder.start(end.getEnd())
                    .end(bDuration)
                    .duration(bDuration.subtract(end.getEnd()))
                    .build();
            periods.add(period);
        }
        return periods;
    }

    // 校验时间格式
    public static void checkTime(String... times) {
        for (String time : times) {
            if (!ReUtil.isMatch(TIME_CHECK_01, time) && !ReUtil.isMatch(TIME_CHECK_02, time)) {
                throw new DataInconsistentException("时间格式不正确");
            }
        }
    }

    // 校验文件格式
    public static void checkFileSource(String... filePaths) {
        for (String filePath : filePaths) {
            if (!StringUtils.hasText(filePath)) {
                throw new DataInconsistentException("文件路径不能为空");
            }
            if (filePath.lastIndexOf(".") < 1 || filePath.length() - 1 == filePath.lastIndexOf(".")) {
                throw new DataInconsistentException("文件路径格式异常,请确认文件路径");
            }
        }
    }

    // 校验视频格式
    public static void checkVideoFilePath(String... filePaths) {
        checkFileSource(filePaths);
        for (String filePath : filePaths) {
            String substring = filePath.substring(filePath.lastIndexOf(".") + 1);
            String suffix = VideoSuffixEnum.suffix(substring);
            if (!StringUtils.hasText(suffix)) {
                throw new DataInconsistentException("不支持该后缀的视频文件");
            }
        }
    }

    /**
     * 生成多路视频宫格合并命令
     * @param inputs     输入视频信息
     * @param baseWidth  基础宽度
     * @param baseHeight 基础高度
     * @param outputPath 输出路径
     * @return String ffmPeg cmd
     * @date 2022/7/25 10:34
     * @author yangzhuo
     */
    public static String multipleMerge(List<VideoInfo> inputs, int baseWidth, int baseHeight, Integer audioIndex, String outputPath) {
        // 基本参数校验
        checkVideoFilePath(outputPath);
        if (CollUtil.isEmpty(inputs)) {
            throw new DataInconsistentException("输入源不能为空");
        }

        // 读取视频信息
        List<VideoInfo> inputInfos = new ArrayList<>();
        for (VideoInfo input : inputs) {
            if (input.getDuration() != 0L) {
                checkVideoFilePath(input.getSource());
                inputInfos.add(getVideoInfo(input.getSource()));
            } else {
                inputInfos.add(VideoInfo.emptyInfo());
            }
        }

        // 设置合适的窗体大小和位置 TODO(现在根据入参设置, 不支持自动调整)
        for (int i = 0; i < inputs.size(); i++) {
            VideoInfo info = inputs.get(i);
            VideoInfo inputInfo = inputInfos.get(i);
            inputInfo.setWidth(info.getWidth());
            inputInfo.setHeight(info.getHeight());
            inputInfo.setX(info.getX());
            inputInfo.setY(info.getY());
        }

        // 视频时长是否一致
        boolean differentDuration = isDifferentDuration(inputInfos);

        // 获取最长视频源长度
        Long maxDuration = null;
        if (differentDuration) {
            maxDuration = inputInfos.stream()
                    .map(VideoInfo::getDuration)
                    .max(Long::compareTo)
                    .orElseThrow(() -> new NullPointerException("视频源时长异常"));
        } else {
            maxDuration = inputInfos.get(0).getDuration();
        }

        // 最长时长是0,说明是无效任务
        if (maxDuration == 0L) {
            throw new DataInconsistentException("拼接任务所有视频时长均为0");
        }

        // cmdBulider
        StringBuilder cmdBuilder = new StringBuilder();

        // 构建输入源
        cmdBuilder.append(" -y");
        for (VideoInfo inputInfo : inputInfos) {
            if (inputInfo.getDuration() != 0L) {
                cmdBuilder.append(String.format(" -ss 0 -to %s -i ", (double) inputInfo.getDuration() / 1000)).append(inputInfo.getSource()).append("  ");
            }
        }

        // 构建过滤器
        cmdBuilder.append(" -filter_complex ");
        // 设置基础窗体
        cmdBuilder.append(String.format("\"nullsrc=size=%sx%s [tmp0]; ", baseWidth, baseHeight));
        // cmdBuilder.append(String.format("\"color=color=Black:size=%sx%s [tmp0]; ", baseWidth, baseHeight));

        for (int i = 0; i < inputInfos.size(); i++) {
            VideoInfo info = inputInfos.get(i);
            // 设置宫格窗体
            if (info.getDuration() != 0L) {
                cmdBuilder.append(
                        String.format("[%s:v] setpts=PTS-STARTPTS, scale=%s:%s:force_original_aspect_ratio=decrease,pad=%s:%s:(ow-iw)/2:(oh-ih)/2,setsar=1 [v%s]; ",
                                i, info.getWidth(), info.getHeight(), info.getWidth(), info.getHeight(), i)
                );
            }

            // 视频长度不一致时,自动填充
            if (differentDuration && maxDuration.compareTo(info.getDuration()) > 0) {
                // 填充时长
                double ex = (double) (maxDuration - info.getDuration()) / 1000;
                // 填充背景
                cmdBuilder.append(
                        // 颜色, 宽度, 高度, 持续时长, 编码
                        String.format("color=color=%s:size=%sx%s:d=%s,format=pix_fmts=%s,",
                                "Black", info.getWidth(), info.getHeight(), ex, "yuv420p")
                );
                // 填充文字
                cmdBuilder.append(
                        // 字体, 文本, x坐标, y坐标, 字体大小, 字体颜色, 阴影
                        String.format("drawtext=fontfile='%s':text='%s':x=%s:y=%s:fontsize=%s:fontcolor=%s:shadowy=%s,",
                                FfmPegExecutor.getFont(), FfmPegExecutor.getNoSourceInfo(), "(w-text_w)/2", "(h-line_h-20)/2", 34, "White", 2)
                );
                // 填充边框
                cmdBuilder.append(
                        // x坐标, y坐标, 宽度, 高度, 颜色
                        String.format("drawbox=drawbox:x=%s:y=%s:w=%s:h=%s:c=%s",
                                0, 0, info.getWidth(), info.getHeight(), "White")
                );
                if (info.getDuration() != 0L) {
                    // 背景变量名
                    cmdBuilder.append(String.format(" [bg%s]; ", i));
                    // 合并视频源 (拼接,先进先出)
                    cmdBuilder.append(String.format("[v%s][bg%s] concat,fifo [v%s]; ", i, i, i));
                } else {
                    // 背景变量名
                    cmdBuilder.append(String.format(" [v%s]; ", i));
                }

            }

            // 设置偏移和输出
            cmdBuilder.append(
                    i + 1 != inputInfos.size() ?
                            String.format("[tmp%s][v%s] overlay=shortest=1:x=%s:y=%s [tmp%s]; ",
                                    i, i, info.getX(), info.getY(), i + 1)
                            :
                            String.format("[tmp%s][v%s] overlay=shortest=1:x=%s:y=%s ",
                                    i, i, info.getX(), info.getY())
            );
        }

        // 音过滤, 指定音频源
        cmdBuilder.append(String.format("\" -map %s:a?", audioIndex));
        // 视频重编码 & 输出到指定目录
        // 执行命令使用的线程数
        cmdBuilder.append(
                // 编码格式, 线程数, 输出目录
                String.format(" -c:v %s -threads %s %s",
                        "libx264", FfmPegExecutor.getThreads(), outputPath)
        );

        // 任务时长信息写入缓存
        FfmPegCache.putProgress(FfmPegExecutor.getTaskId(),
                MutablePair.of(DateUtil.format(DateUtil.date(maxDuration).offset(DateField.HOUR, -8), "HH:mm:ss.SSS"),
                        ""));
        return cmdBuilder.toString();
    }

    /**
     * 校验视频长度是否一致
     * @param inputInfos 输入的视频信息
     * @return boolean
     * @date 2022/7/27 9:48
     * @author yangzhuo
     */
    private static boolean isDifferentDuration(List<VideoInfo> inputInfos) {
        Long duration = inputInfos.get(0).getDuration();
        for (int i = 1; i < inputInfos.size(); i++) {
            if (duration.compareTo(inputInfos.get(i).getDuration()) != 0) {
                return true;
            }
        }
        return false;
    }

}

package com.ulearning.video.editor.controller;

import com.ulearning.video.common.utils.R;
import com.ulearning.video.editor.fo.CatchPictureFo;
import com.ulearning.video.editor.fo.MultipleMergeFo;
import com.ulearning.video.editor.service.VideoEditService;
import com.ulearning.video.ffmpeg.entity.ProgressInfo;
import com.ulearning.video.ffmpeg.entity.TaskInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 视频编辑控制器
 * @date 2022-07-28 11:18
 */
@Api(tags = "视频编辑控制器")
@Slf4j
@RestController
@RequestMapping("editor")
public class EditorController {

    @Resource
    private VideoEditService videoEditService;

    @ApiOperation("视频合成(多宫格拼接)")
    @PostMapping("multipleMerge")
    public R<TaskInfo> multipleMerge(@RequestBody MultipleMergeFo multipleMergeFo) {
        return R.success(videoEditService.multipleMerge(multipleMergeFo));
    }

    @ApiOperation("获取视频指定时间的图片(合并请求, 同步执行)")
    @PostMapping("catchPicture")
    public void catchPicture(@RequestBody CatchPictureFo catchPictureFo, HttpServletResponse resp) throws IOException {
        videoEditService.catchPicture(catchPictureFo, resp);
    }

    @ApiOperation("获取视频指定时间的图片(合并请求, 同步执行)")
    @GetMapping("catchPicture")
    public void catchPicture(@ApiParam(value = "视频源地址", required = true) @RequestParam("source") String source,
                             @ApiParam(value = "截取的时间点") @RequestParam(value = "duration", defaultValue = "0") Long duration,
                             HttpServletResponse resp) throws IOException {
        CatchPictureFo catchPictureFo = new CatchPictureFo();
        catchPictureFo.setSource(source);
        catchPictureFo.setDuration(duration);
        videoEditService.catchPicture(catchPictureFo, resp);
    }

    @ApiOperation("查询任务执行进度")
    @GetMapping("executeProgress/{taskId}")
    public R<ProgressInfo> getExecuteProgress(@ApiParam(value = "任务id", required = true) @PathVariable("taskId") Integer taskId) {
        ProgressInfo progress = videoEditService.getExecuteProgress(taskId);
        if (Objects.isNull(progress)) {
            return R.error("任务信息不存在");
        }
        return R.success(progress);
    }

    @ApiOperation("取消未开始执行的编辑任务")
    @GetMapping("cancelTask/{taskId}")
    public R<Void> cancelTask(@ApiParam(value = "任务id", required = true) @PathVariable("taskId") Integer taskId) {
        boolean flag = videoEditService.cancelTask(taskId);
        if (flag) {
            return R.success();
        }
        return R.fail();
    }

}

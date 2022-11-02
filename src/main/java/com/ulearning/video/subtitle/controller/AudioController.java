package com.ulearning.video.subtitle.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.alibaba.fastjson.JSONObject;
import com.ulearning.video.common.utils.MybatisBatchUtils;
import com.ulearning.video.common.utils.R;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleDetail;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleObject;
import com.ulearning.video.ffmpeg.dao.VideoEditRecordDao;
import com.ulearning.video.ffmpeg.model.VideoEditRecordModel;
import com.ulearning.video.subtitle.fo.SubtitleFo;
import com.ulearning.video.subtitle.service.AudioService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yangzhuo
 * @description 音频编辑器
 * @date 2022-09-21 10:52
 */
@Slf4j
@Api(tags = "音频控制器")
@RestController
@RequestMapping("/audio")
public class AudioController {

    @Autowired
    private AudioService audioService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private VideoEditRecordDao videoEditRecordDao;

    @PostMapping("createSubtitleTask")
    public R<Integer> createSubtitleTask(@RequestBody() SubtitleFo subtitleFo) {
        Integer taskId = audioService.subtitleTask(subtitleFo);
        if (Objects.nonNull(taskId)) {
            return R.success(taskId);
        }
        return R.error("创建字幕生成任务失败");
    }

    @GetMapping("test")
    public String test() {
        // Aggregation aggregation = Aggregation.newAggregation(
        //         //先查询父集合
        //         Aggregation.match(Criteria.where("taskId").is(1L)),
        //         //拆分子集合字段
        //         Aggregation.unwind("subtitleDetails"),
        // );
        //
        // AggregationResults<JSONObject> aggregate = mongoTemplate.aggregate(aggregation, SubtitleObject.class, JSONObject.class);
        // List<JSONObject> mappedResults = aggregate.getMappedResults();
        // System.out.println(mappedResults.size());
        // System.out.println(mappedResults);
        // Query query = new Query(Criteria.where("taskId").is(1L));
        // query.fields().include("subtitleDetails");
        // SubtitleObject one = mongoTemplate.find(query, SubtitleDetail.class,SubtitleObject.class);
        // System.out.println(one);

        // uodate
        // List<VideoEditRecordModel> arr = new ArrayList<>();
        // int insertSize = 10000;
        // for (int i = 0; i < 10000; i++) {
        //     arr.add(VideoEditRecordModel.creat("test", "test"));
        // }
        // TimeInterval timer = DateUtil.timer();
        // int result = MybatisBatchUtils.batchUpdateOrInsert(arr, VideoEditRecordDao.class, (d, m) -> m.insert(d));
        // log.info("insert:{} success:{} costTime: {}", insertSize, result, timer.intervalRestart());
        // Criteria criteria = new Criteria();
        // criteria.and("taskId").is(1L);
        // criteria.and("subtitleDetails.index").is(1);
        // Query query = new Query(criteria);
        // //更新内容
        // Update update = new Update();
        // update.set("subtitleDetails.$.sentences","测试修改");
        // UpdateResult updateResult = mongoTemplate.updateFirst(query, update, SubtitleObject.class);
        // System.out.println(updateResult.getMatchedCount());
        // System.out.println(updateResult.getModifiedCount());


        // List<SubtitleDetail> subtitleDetails = Arrays.asList(
        //         new SubtitleDetail(0, "test1", "10", "20"),
        //         new SubtitleDetail(1, "test2", "30", "40"),
        //         new SubtitleDetail(2, "test3", "50", "60"),
        //         new SubtitleDetail(3, "test4", "70", "80")
        // );
        // SubtitleObject test = SubtitleObject.builder()
        //         .title("测试字幕信息类")
        //         .taskId(1L)
        //         .createTime(new Date())
        //         .subtitleDetails(subtitleDetails)
        //         .build();
        // SubtitleObject insert = mongoTemplate.insert(test);
        // System.out.println(insert);
        // String taskId = null;
        // Integer result = audioService.videoToAudio("D:\\test\\赵雷-成都(超清).mp4", "D:\\test\\myMP3.mp3");
        // if (FfmpegUtil.CODE_SUCCESS.equals(result)) {
        //     String fileUrl = UploadFactory.getInstance().uploadFile("resources/audio/myMP3.mp3", FileUtil.file("D:\\test\\myMP3.mp3"));
        //     taskId = AudioSubtitleFactory.getInstance().audioSubtitle(fileUrl, null);
        //     System.out.println(taskId);
        // }
        return "111";
    }
}

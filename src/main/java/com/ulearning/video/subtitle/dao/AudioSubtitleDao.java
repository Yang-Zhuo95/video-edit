package com.ulearning.video.subtitle.dao;

import com.ulearning.video.subtitle.model.AudioSubtitleModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangzhuo
 * @description 字幕转换dao
 * @date 2022-09-22 11:10
 */
@Repository
public interface AudioSubtitleDao {

    /**
     * 新增字幕转换任务
     * @param audioSubtitleModel 字幕转换任务记录
     * @return int  主键id
     * @date 2022/9/22 11:15
     * @author yangzhuo
     */
    int insert(AudioSubtitleModel audioSubtitleModel);

    /**
     * 更新字幕转换任务
     * @param audioSubtitleModel 字幕转换任务记录
     * @return int 执行成功与否 0-失败 1- 成功
     * @date 2022/7/29 13:21
     * @author yangzhuo
     */
    int update(AudioSubtitleModel audioSubtitleModel);


    /**
     * 通过主键更新视频编辑任务执行状态
     * @param id        主键id
     * @param oldStatus 旧值
     * @param newStatus 新值
     * @return int 执行成功与否 0-失败 1- 成功
     * @date 2022/7/29 13:50
     * @author yangzhuo
     */
    int updateStatus(@Param("id") int id, @Param("oldStatus") int oldStatus, @Param("newStatus") int newStatus);

    /**
     * 查询执行任务信息
     * @param id 任务id
     * @return AudioSubtitleModel
     * @date 2022/8/2 15:07
     * @author yangzhuo
     */
    AudioSubtitleModel findById(@Param("id") Integer id);

    /**
     * 批量查询需要轮询的字幕生成任务
     * @param maxSize 一次查询的条数
     * @return List<AudioSubtitleModel> 字幕生成任务列表
     * @date 2022/9/22 15:04
     * @author yangzhuo
     */
    List<AudioSubtitleModel> findNeedPollingList(@Param("maxSize") Integer maxSize);
}

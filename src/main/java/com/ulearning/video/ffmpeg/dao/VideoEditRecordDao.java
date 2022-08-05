package com.ulearning.video.ffmpeg.dao;

import com.ulearning.video.ffmpeg.model.VideoEditRecordModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author yangzhuo
 * @description 视频编辑dao
 * @date 2022-07-29 9:49
 */
@Repository
public interface VideoEditRecordDao {
    /**
     * 新增视频编辑任务
     * @param videoEditRecordModel 视频编辑任务记录
     * @return int 主键id
     * @date 2022/7/29 13:21
     * @author yangzhuo
     */
    int insert(VideoEditRecordModel videoEditRecordModel);

    /**
     * 更新视频编辑任务
     * @param videoEditRecordModel 视频编辑任务记录
     * @return int 执行成功与否 0-失败 1- 成功
     * @date 2022/7/29 13:21
     * @author yangzhuo
     */
    int update(VideoEditRecordModel videoEditRecordModel);

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
     * @param taskId 任务id
     * @return VideoEditModel
     * @date 2022/8/2 15:07
     * @author yangzhuo
     */
    VideoEditRecordModel findById(@Param("taskId") Integer taskId);
}

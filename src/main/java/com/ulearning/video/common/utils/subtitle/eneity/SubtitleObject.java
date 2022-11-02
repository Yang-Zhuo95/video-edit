package com.ulearning.video.common.utils.subtitle.eneity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * @author yangzhuo
 * @description 字幕任务对象
 * @date 2022-09-22 15:37
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document("audio_subtitle_doc")
public class SubtitleObject {

    private ObjectId id;

    private Long taskId;

    private String text;

    private List<SubtitleDetail> subtitleDetails;

    private Date createTime;
}

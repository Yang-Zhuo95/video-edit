package com.ulearning.video.ffmpeg.task;

import com.ulearning.video.ffmpeg.actuator.Actuator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * @author yangzhuo
 * @description future任务类
 * @date 2022-08-08 13:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FutureModel<T extends Actuator, R> {
    private T actuator;
    private CompletableFuture<R> future;
}

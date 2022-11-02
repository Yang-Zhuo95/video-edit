package com.ulearning.video.common.utils.subtitle.yitu;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ulearning.video.common.utils.subtitle.AudioSubtitleService;
import com.ulearning.video.common.utils.subtitle.config.YiTuConfig;
import com.ulearning.video.common.utils.subtitle.eneity.Result;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleDetail;
import com.ulearning.video.common.utils.subtitle.eneity.SubtitleObject;
import com.ulearning.video.common.utils.subtitle.yitu.model.PublicAudioV4ReqEntity;
import com.ulearning.video.common.utils.subtitle.yitu.state.State;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author yangzhuo
 * @description 依图语音转字幕服务
 * @date 2022-09-21 17:10
 */
@Slf4j
@Service
public class YiTuAudioSubtitleServiceImpl implements AudioSubtitleService {

    private String serviceUrl;

    private String accessId;

    private String accessKey;

    @Autowired
    private YiTuConfig yiTuConfig;

    @Override
    public String serverName() {
        return yiTuConfig.configured() ? "yitu" : null;
    }

    @PostConstruct
    private void init() {
        if (yiTuConfig.configured()) {
            serviceUrl = yiTuConfig.serviceUrl;
            accessId = yiTuConfig.accessId;
            accessKey = yiTuConfig.accessKey;
        }
    }

    @Override
    public String audioSubtitle(String audioPath, String callback) {
        PublicAudioV4ReqEntity audioV4ReqEntity = PublicAudioV4ReqEntity.builder()
                .audioUrl(audioPath)
                .callback(callback)
                .build();
        Map<String, String> signature = getSignature(accessId, accessKey);
        JSONObject content = null;
        String path = serviceUrl + "v4/lasr";
        MediaType jsonType = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String jsonStr = JSON.toJSONString(audioV4ReqEntity);
        RequestBody body = RequestBody.create(jsonStr, jsonType);
        Request request = new Request.Builder().url(path)
                .addHeader("Content-Type", "application/json;charset=utf8")
                .addHeader("x-dev-id", signature.get("x-dev-id"))
                .addHeader("x-signature", signature.get("x-signature"))
                .addHeader("x-request-send-timestamp", signature.get("x-request-send-timestamp"))
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            content = JSON.parseObject(Objects.requireNonNull(response.body()).string());
        } catch (Exception e) {
            log.error("process error: IOException: ", e);
            return null;
        }
        return content.getString("taskId");
    }

    @Override
    public Result queryTask(String taskId) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("<Content-Type>");
        RequestBody body = RequestBody.create(mediaType, "");
        String path = serviceUrl + "v4/lasr/" + taskId;
        Map<String, String> signature = getSignature(accessId, accessKey);
        Request request = new Request.Builder()
                .url(path)
                .addHeader("Content-Type", "application/json;charset=utf8")
                .addHeader("x-dev-id", signature.get("x-dev-id"))
                .addHeader("x-signature", signature.get("x-signature"))
                .addHeader("x-request-send-timestamp", signature.get("x-request-send-timestamp"))
                .get()
                .build();
        Result result = Result.builder().code(Result.RUNNING).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = JSON.parseObject(Objects.requireNonNull(response.body()).string());
            Optional<JSONObject> optional = Optional.ofNullable(jsonObject);
            // 响应成功
            Optional<JSONObject> returnSuccess = optional.filter(object -> State.RTN_SUCCESS.equals(object.getInteger("rtn")));
            if (!returnSuccess.isPresent()) {
                log.error("请求响应失败, 响应状态码不为{}", State.RTN_SUCCESS);
                return Result.error();
            }

            // 响应成功, 读取失败(文件格式错误/资源不存在)
            Optional<JSONObject> loadingFail = returnSuccess.filter(object -> State.TFILE_LOADING_FAILD.equals(object.getJSONObject("data").getInteger("statusCode")));
            if (loadingFail.isPresent()) {
                log.error("源文件加载失败, 确认文件url或格式是否正确, statusCode: {}", State.TFILE_LOADING_FAILD);
                return Result.error();
            }

            // 响应成功, 转写完成
            Optional<JSONObject> taskSuccess = returnSuccess.filter(object -> State.TASK_SUCC.equals(object.getJSONObject("data").getInteger("statusCode")));
            if (taskSuccess.isPresent()) {
                // 响应结果成功
                result.setCode(Result.SUCCESS);
                // 取出响应的字幕信息列表
                Optional<JSONObject> detail = taskSuccess.filter(o -> CollUtil.isNotEmpty(o.getJSONObject("data").getJSONObject("speechResult").getJSONArray("detail")));
                if (!detail.isPresent()) {
                    log.warn("字幕转写成功, 字幕信息列表为空");
                    result.setMsg("字幕转写成功, 字幕信息列表为空");
                    return result;
                }
                // 对字幕信息列表进行统一处理
                result.setSubtitleObject(this.json2Subtitle(detail.get().getJSONObject("data").getJSONObject("speechResult")));
                return result;
            }

            // 其它情况, 官方文档未给出对应场景
            log.info("其它类型响应, 响应结果为: {}", jsonObject);
        } catch (Exception e) {
            log.error("process error: IOException: ", e);
            return result;
        }
        return result;
    }

    public Map<String, String> getSignature(String devId, String devKey) {
        long timestamp = System.currentTimeMillis() / 1000L;
        String signKey = devId + timestamp;

        SecretKeySpec signingKey = new SecretKeySpec(devKey.getBytes(), "HmacSHA256");
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
        } catch (Exception e) {
            log.error("process error: Exception: ", e);
        }
        byte[] bytes = mac.doFinal(signKey.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String sig = formatter.toString();

        Map<String, String> map = new HashMap<>();
        map.put("x-dev-id", devId);
        map.put("x-signature", sig);
        map.put("x-request-send-timestamp", String.valueOf(timestamp));
        return map;
    }

    @Override
    public boolean configured() {
        return yiTuConfig.configured();
    }

    /**
     * 将Json响应体中的字幕信息结果进行统一转换处理
     * @param object Json响应体
     * @return SubtitleObject   统一处理后的字幕信息
     * @date 2022/9/26 9:52
     * @author yangzhuo
     */
    private SubtitleObject json2Subtitle(JSONObject object) {
        SubtitleObject subtitleObject = new SubtitleObject();
        subtitleObject.setCreateTime(new Date());
        // 字幕文本内容
        subtitleObject.setText(object.getString("resultText"));
        // 对字幕信息进行统一格式化, 落库mongo
        JSONArray detail = object.getJSONArray("detail");

        subtitleObject.setSubtitleDetails(IntStream.range(0, detail.size())
                .mapToObj(i -> this.object2SubtitleDetail(i, detail.getJSONObject(i)))
                .collect(Collectors.toList()));
        return subtitleObject;
    }

    private SubtitleDetail object2SubtitleDetail(Integer index, JSONObject object) {
        SubtitleDetail subtitleDetail = new SubtitleDetail();
        // 下标
        subtitleDetail.setIndex(index);

        // 字幕内容
        String sentences = object.getString("sentences");

        // 因为返回的字幕最后带有标点符号, 需要清理掉 '，' 和 '。'
        if (sentences.endsWith("，") || sentences.endsWith("。")) {
            sentences = sentences.substring(0, sentences.length() - 1);
        }
        subtitleDetail.setSentences(sentences);

        // 字幕开始 & 结束时间
        subtitleDetail.setStartTime(object.getString("startTime"));
        subtitleDetail.setEndTime(object.getString("endTime"));
        return subtitleDetail;
    }
}

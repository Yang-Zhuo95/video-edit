package com.ulearning.video.ffmpeg.enums;

/**
 * @description 视频后缀枚举类
 * @date 2022/7/27 14:44
 * @author yangzhuo
 */
public enum VideoSuffixEnum {
    avi(".avi"),
    mov(".mov"),
    mpeg(".mpeg"),
    mpe(".mpe"),
    dat(".dat"),
    vob(".vob"),
    asf(".asf"),
    mp4(".mp4"),
    wmv(".wmv"),
    ;
    private String suffix;

    VideoSuffixEnum(String suffix) {
        this.suffix = suffix;
    }

    public static String suffix(String suffixName) {
        for (VideoSuffixEnum typeEnum : VideoSuffixEnum.values()) {
            if (suffixName.equals(typeEnum.name())) {
                return typeEnum.suffix;
            }
        }
        return null;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}

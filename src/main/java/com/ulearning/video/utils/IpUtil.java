package com.ulearning.video.utils;

import cn.hutool.core.util.ReUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author yangzhuo
 * @description ip地址工具类
 * @date 2022-08-04 10:08
 */
@Slf4j
public class IpUtil {
    /**
     * 根据匹配内容,返回本机ip
     * @param regex 匹配内容
     * @return String ip地址
     * @date 2022/8/4 10:19
     * @author yangzhuo
     */
    public static String matchHostIp(String regex) {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            Enumeration<InetAddress> addresses;
            Pattern pattern = Pattern.compile(regex);
            while (networks.hasMoreElements()) {
                addresses = networks.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    //由于线上一台机器可能在多个网段，根据配置项匹配对应ip
                    if (inetAddress instanceof Inet4Address
                            && inetAddress.isSiteLocalAddress()
                            && ReUtil.isMatch(pattern, inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress();

                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // 默认地址
        String tempIp = "127.0.6.1";
        try {
            tempIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            //ignore return tempIP;
            log.error(e.getMessage());
        }
        return tempIp;
    }
}
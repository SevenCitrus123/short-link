package project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

import static project.common.constant.RedisKeyConstant.DEFAULT_CACHE_VALID_TIME;

public class LinkUtil {

    public static long getLinkCacheValidTime(Date validDate) {
        if (validDate == null) {
            return DEFAULT_CACHE_VALID_TIME;
        }
        long remainMs = DateUtil.between(new Date(), validDate, DateUnit.MS, false);
        return Math.max(remainMs, 0L);
    }

    public static String getActualIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac")) {
            return "Mac OS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        }
        return "Unknown";
    }

    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent.contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.contains("opera")) {
            return "Opera";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        }
        return "Unknown";
    }

    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        return userAgent.contains("mobile") ? "Mobile" : "PC";
    }

    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getActualIp(request);
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile";
    }
}
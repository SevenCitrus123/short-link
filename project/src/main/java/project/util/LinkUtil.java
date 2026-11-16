package project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;

import static project.common.constant.RedisKeyConstant.DEFAULT_CACHE_VALID_TIME;

public class LinkUtil {

    /**
     * 计算缓存的存活时间（毫秒）
     * 永久有效 -> 默认 1 小时
     * 限时有效 -> 剩余有效期
     */
    public static long getLinkCacheValidTime(Date validDate) {
        if (validDate == null) {
            return DEFAULT_CACHE_VALID_TIME;
        }
        long remainMs = DateUtil.between(new Date(), validDate, DateUnit.MS, false);
        return Math.max(remainMs, 0L);
    }
}

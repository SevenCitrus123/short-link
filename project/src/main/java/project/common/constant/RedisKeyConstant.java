package project.common.constant;

public class RedisKeyConstant {

    /**
     * 短链接跳转缓存
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接不存在标记（防穿透）
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is-null:goto:%s";

    /**
     * 短链接跳转分布式锁（防击穿）
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    /**
     * 默认缓存时长：1 小时（毫秒）
     */
    public static final long DEFAULT_CACHE_VALID_TIME = 60 * 60 * 1000L;

    public static final long GOTO_IS_NULL_CACHE_TIME = 30 * 60 * 1000L;

    public static final long GOTO_EXPIRED_CACHE_TIME = 5 * 60 * 1000L;
}
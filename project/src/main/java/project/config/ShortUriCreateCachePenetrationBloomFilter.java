package project.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ShortUriCreateCachePenetrationBloomFilter {

    /**
     * 防止查询fullShortLink时查询数据库的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> shortUriCreateBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("Project:FullShortLink");
        boolean initialized = cachePenetrationBloomFilter.tryInit(100000000L, 0.001);
        if (!initialized) {
            log.warn("布隆过滤器 [Project:FullShortLink] 在 Redis 中已存在，沿用已有配置。"
                    + "若历史配置的容量或误判率与本代码不一致，需要先删除该 key 再重启");
        }
        return cachePenetrationBloomFilter;
    }
}

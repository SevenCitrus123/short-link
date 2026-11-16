package project.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import project.dao.entity.ShortLinkDO;
import project.dao.mapper.ShortLinkMapper;
import project.util.LinkUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkCachePreheatRunner implements ApplicationRunner {

    private final ShortLinkMapper shortLinkMapper;

    private final RBloomFilter<String> bloomFilter;

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${short-link.cache.preheat.enable:true}")
    private boolean preheatEnable;

    @Value("${short-link.cache.preheat.limit:1000}")
    private int preheatLimit;

    @Override
    public void run(ApplicationArguments args) {
        if (!preheatEnable) {
            return;
        }
        try {
            List<ShortLinkDO> shortLinkDOList = shortLinkMapper.selectList(
                    Wrappers.lambdaQuery(ShortLinkDO.class)
                            .eq(ShortLinkDO::getEnableStatus, 0)
                            .orderByDesc(ShortLinkDO::getCreateTime)
                            .last("limit " + preheatLimit));
            int successCount = 0;
            for (ShortLinkDO each : shortLinkDOList) {
                long cacheMs = LinkUtil.getLinkCacheValidTime(each.getValidDate());
                if (cacheMs <= 0) {
                    continue;
                }
                try {
                    stringRedisTemplate.opsForValue().set(
                            String.format(GOTO_SHORT_LINK_KEY, each.getFullShortUrl()),
                            each.getOriginUrl(),
                            cacheMs,
                            TimeUnit.MILLISECONDS);
                    bloomFilter.add(each.getFullShortUrl());
                    successCount++;
                } catch (Exception ex) {
                    log.warn("短链接启动预热失败，fullShortUrl={}", each.getFullShortUrl(), ex);
                }
            }
            log.info("短链接启动预热完成，候选 {} 条，成功 {} 条", shortLinkDOList.size(), successCount);
        } catch (Exception ex) {
            log.warn("短链接启动预热整体失败", ex);
        }
    }
}

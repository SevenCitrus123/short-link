package project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import project.common.convention.exception.ClientException;
import project.common.convention.exception.ServiceException;
import project.common.enums.ShortLinkErrorCodeEnum;
import project.dao.entity.ShortLinkDO;
import project.dao.entity.ShortLinkGotoDO;
import project.dao.mapper.ShortLinkGotoMapper;
import project.dao.mapper.ShortLinkMapper;
import project.dto.req.ShortLinkCreateReqDTO;
import project.dto.req.ShortLinkGroupCountQueryRespDTO;
import project.dto.req.ShortLinkPageReqDTO;
import project.dto.resp.ShortLinkCreateRespDTO;
import project.dto.resp.ShortLinkPageRespDTO;
import project.service.ShortLinkService;
import project.util.HashUtil;
import project.util.LinkUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static project.common.constant.RedisKeyConstant.*;
import static project.service.impl.UrlTitleServiceImpl.USER_AGENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private static final int MAX_GENERATE_COUNT = 5;

    private final RBloomFilter<String> bloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    @Value("${short-link.domain.default:www.baidu.com}")
    private String defaultDomain;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        isValid(requestParam);
        String suffix = generateSuffix(requestParam);
        String fullShortUrl = buildFullShortUrl(defaultDomain, suffix);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(defaultDomain)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                        .shortUri(suffix)
                        .fullShortUrl(fullShortUrl)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(requestParam.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .enableStatus(0)
                        .clickNum(0).totalPv(0).totalUv(0).totalUip(0).delFlag(0).delTime(0L)
                        .build();
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ShortLinkErrorCodeEnum.SHORT_LINK_ALREADY_EXIST);
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    preheatCache(fullShortUrl, requestParam.getOriginUrl(), requestParam.getValidDate());
                }
            });
        } else {
            preheatCache(fullShortUrl, requestParam.getOriginUrl(), requestParam.getValidDate());
        }

        return BeanUtil.copyProperties(shortLinkDO, ShortLinkCreateRespDTO.class);
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        if (StrUtil.isBlank(requestParam.getGid())) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByAsc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> BeanUtil.copyProperties(each, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String fullShortUrl = buildFullShortUrl(defaultDomain, shortUri);
        String gotoKey = String.format(GOTO_SHORT_LINK_KEY, fullShortUrl);
        String isNullKey = String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl);

        // 第 1 步：缓存命中，直接跳（绝大多数请求走这里）
        String originUrl = stringRedisTemplate.opsForValue().get(gotoKey);
        if (StrUtil.isNotBlank(originUrl)) {
            httpServletResponse.sendRedirect(originUrl);
            return;
        }

        // 第 2 步：命中"不存在"标记，直接 404（防穿透）
        if (StrUtil.isNotBlank(stringRedisTemplate.opsForValue().get(isNullKey))) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        // 第 3 步：布隆过滤器判存在（防穿透，见下方注意事项）
        if (!bloomFilter.contains(fullShortUrl)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        // 第 4 步：加锁，只放一个线程去查库（防击穿）
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 第 5 步：双检——可能别的线程刚把缓存写好了
            originUrl = stringRedisTemplate.opsForValue().get(gotoKey);
            if (StrUtil.isNotBlank(originUrl)) {
                httpServletResponse.sendRedirect(originUrl);
                return;
            }
            if (StrUtil.isNotBlank(stringRedisTemplate.opsForValue().get(isNullKey))) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }

            // 第 6 步：查路由表拿 gid
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(
                    Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                            .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl));
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(isNullKey, "1", GOTO_IS_NULL_CACHE_TIME, TimeUnit.MILLISECONDS);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }

            // 第 7 步：用 gid 精准查 t_link
            ShortLinkDO shortLinkDO = baseMapper.selectOne(
                    Wrappers.lambdaQuery(ShortLinkDO.class)
                            .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                            .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                            .eq(ShortLinkDO::getEnableStatus, 0)
                            .eq(ShortLinkDO::getDelFlag, 0));

            if (shortLinkDO == null || StrUtil.isBlank(shortLinkDO.getOriginUrl())) {
                stringRedisTemplate.opsForValue().set(isNullKey, "1", GOTO_IS_NULL_CACHE_TIME, TimeUnit.MILLISECONDS);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            if (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())) {
                log.info("短链接已过期，fullShortUrl={}", fullShortUrl);
                stringRedisTemplate.opsForValue().set(isNullKey, "2", GOTO_EXPIRED_CACHE_TIME, TimeUnit.MILLISECONDS);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }

            preheatCache(fullShortUrl, shortLinkDO.getOriginUrl(), shortLinkDO.getValidDate());

            httpServletResponse.sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }


    private void isValid(ShortLinkCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (requestParam.getOriginUrl() == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_NULL);
        }
        if (requestParam.getDomain() == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_DOMAIN_NULL);
        }
        Integer validDateType = requestParam.getValidDateType();
        Date validDate = requestParam.getValidDate();
        if (validDateType == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (validDateType == 0 && validDate != null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (validDateType == 1 && (validDate == null || validDate.before(new Date()))) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (requestParam.getDescribe() == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (requestParam.getDescribe().length() > 1024) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_DESCRIBE_TOO_LONG);
        }
    }

    public Boolean hasFullShortUrl(String fullShortUrl) {
        if (!bloomFilter.contains(fullShortUrl)) {
            return false;
        }
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        return shortLinkDO != null;
    }


    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        for (int i = 0; i < MAX_GENERATE_COUNT; i++) {
            String originUrlWithSalt = requestParam.getOriginUrl() + UUID.randomUUID();
            String shortUri = HashUtil.hashToBase62(originUrlWithSalt);
            String fullShortUrl = buildFullShortUrl(defaultDomain, shortUri);
            if (!hasFullShortUrl(fullShortUrl)) {
                bloomFilter.add(fullShortUrl);
                return shortUri;
            }
        }
        throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_TRYS_UP_TO_5);
    }

    private String buildFullShortUrl(String domain, String shortUri) {
        return "http://" + domain + "/" + shortUri;
    }


    private void preheatCache(String fullShortUrl, String originUrl, Date validDate) {
        long cacheMs = LinkUtil.getLinkCacheValidTime(validDate);
        if (cacheMs <= 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    originUrl,
                    cacheMs,
                    TimeUnit.MILLISECONDS);
            // 这条短链之前可能被用户"撞"过，留了 is-null 标记，创建成功后必须清掉
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        } catch (Exception ex) {
            log.warn("短链接缓存预热失败，fullShortUrl={}", fullShortUrl, ex);
        }
    }

    private String getFavicon(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(3000)
                    .userAgent(USER_AGENT)
                    .followRedirects(true)
                    .execute();
            if (response.statusCode() != 200) {
                return null;
            }
            Document document = response.parse();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink == null) {
                return null;
            }
            String favicon = faviconLink.attr("abs:href");
            if (StrUtil.isBlank(favicon) || favicon.length() > 250) {
                return null;
            }
            return favicon;
        } catch (Exception ex) {
            log.warn("获取网站图标失败，url={}", url, ex);
            return null;
        }
    }
}

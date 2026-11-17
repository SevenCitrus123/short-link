package project.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import project.dao.entity.LinkAccessLogsDO;
import project.dao.entity.LinkAccessStatsDO;
import project.dao.entity.LinkBrowserStatsDO;
import project.dao.entity.LinkLocaleStatsDO;
import project.dao.entity.LinkOsStatsDO;
import project.dao.entity.LinkStatsTodayDO;
import project.dao.entity.ShortLinkGotoDO;
import project.dao.mapper.LinkAccessLogsMapper;
import project.dao.mapper.LinkAccessStatsMapper;
import project.dao.mapper.LinkBrowserStatsMapper;
import project.dao.mapper.LinkLocaleStatsMapper;
import project.dao.mapper.LinkOsStatsMapper;
import project.dao.mapper.LinkStatsTodayMapper;
import project.dao.mapper.ShortLinkGotoMapper;
import project.dao.mapper.ShortLinkMapper;
import project.dto.biz.ShortLinkStatsRecordDTO;
import project.service.ShortLinkStatsSaveService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static project.common.constant.ShortLinkConstant.UNKNOWN_LOCALE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkStatsSaveServiceImpl implements ShortLinkStatsSaveService {

    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final ShortLinkMapper shortLinkMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    @Value("${short-link.stats.locale.amap-key:}")
    private String statsLocaleAmapKey;

    @Async("statsExecutor")
    @Override
    public void saveStats(ShortLinkStatsRecordDTO statsRecord) {
        try {
            String fullShortUrl = statsRecord.getFullShortUrl();
            ShortLinkGotoDO gotoDO = shortLinkGotoMapper.selectOne(
                    Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                            .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl));
            if (gotoDO == null) {
                return;
            }
            Date now = statsRecord.getCurrentDate();
            int hour = DateUtil.hour(now, true);
            int weekday = DateUtil.dayOfWeekEnum(now).getIso8601Value();
            int uv = Boolean.TRUE.equals(statsRecord.getUvFirstFlag()) ? 1 : 0;
            int uip = Boolean.TRUE.equals(statsRecord.getUipFirstFlag()) ? 1 : 0;

            linkAccessStatsMapper.shortLinkStats(LinkAccessStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(now)
                    .hour(hour)
                    .weekday(weekday)
                    .pv(1).uv(uv).uip(uip)
                    .build());

            linkStatsTodayMapper.shortLinkTodayState(LinkStatsTodayDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(now)
                    .todayPv(1).todayUv(uv).todayUip(uip)
                    .build());

            linkOsStatsMapper.shortLinkOsState(LinkOsStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(now)
                    .cnt(1)
                    .os(StrUtil.blankToDefault(statsRecord.getOs(), UNKNOWN_LOCALE))
                    .build());

            linkBrowserStatsMapper.shortLinkBrowserState(LinkBrowserStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(now)
                    .cnt(1)
                    .browser(StrUtil.blankToDefault(statsRecord.getBrowser(), UNKNOWN_LOCALE))
                    .build());

            linkAccessLogsMapper.insert(LinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .user(statsRecord.getUv())
                    .ip(statsRecord.getRemoteAddr())
                    .browser(statsRecord.getBrowser())
                    .os(statsRecord.getOs())
                    .device(statsRecord.getDevice())
                    .network(statsRecord.getNetwork())
                    .build());

            String province = UNKNOWN_LOCALE;
            String city = UNKNOWN_LOCALE;
            String adcode = UNKNOWN_LOCALE;
            if (StrUtil.isNotBlank(statsLocaleAmapKey)) {
                try {
                    Map<String, Object> localeParamMap = new HashMap<>();
                    localeParamMap.put("key", statsLocaleAmapKey);
                    localeParamMap.put("ip", statsRecord.getRemoteAddr());
                    String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
                    JSONObject localeResultObj = JSON.parseObject(localeResultStr);
                    if ("10000".equals(localeResultObj.getString("infocode"))) {
                        String provinceResult = localeResultObj.getString("province");
                        if (StrUtil.isNotBlank(provinceResult) && !"[]".equals(provinceResult)) {
                            province = provinceResult;
                            city = normalize(localeResultObj.getString("city"));
                            adcode = normalize(localeResultObj.getString("adcode"));
                        }
                    }
                } catch (Exception ex) {
                    log.warn("获取短链接访问地区失败，ip={}", statsRecord.getRemoteAddr(), ex);
                }
            }
            linkLocaleStatsMapper.shortLinkLocaleState(LinkLocaleStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .date(now)
                    .cnt(1)
                    .country("中国")
                    .province(province)
                    .city(city)
                    .adcode(adcode)
                    .build());

            shortLinkMapper.incrementStats(gotoDO.getGid(), fullShortUrl, 1, uv, uip);
        } catch (Exception ex) {
            log.error("短链接统计保存失败", ex);
        }
    }

    private String normalize(String value) {
        return StrUtil.isBlank(value) || "[]".equals(value) ? UNKNOWN_LOCALE : value;
    }
}

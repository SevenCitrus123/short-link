package project.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import project.dao.entity.LinkAccessLogsDO;
import project.dao.entity.LinkAccessStatsDO;
import project.dao.entity.LinkStatsTodayDO;
import project.dao.entity.ShortLinkGotoDO;
import project.dao.mapper.LinkAccessLogsMapper;
import project.dao.mapper.LinkAccessStatsMapper;
import project.dao.mapper.LinkStatsTodayMapper;
import project.dao.mapper.ShortLinkGotoMapper;
import project.dao.mapper.ShortLinkMapper;
import project.dto.biz.ShortLinkStatsRecordDTO;
import project.service.ShortLinkStatsSaveService;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkStatsSaveServiceImpl implements ShortLinkStatsSaveService {

    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final ShortLinkMapper shortLinkMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;

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

            linkAccessLogsMapper.insert(LinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .user(statsRecord.getUv())
                    .ip(statsRecord.getRemoteAddr())
                    .browser(statsRecord.getBrowser())
                    .os(statsRecord.getOs())
                    .device(statsRecord.getDevice())
                    .network(statsRecord.getNetwork())
                    .build());

            shortLinkMapper.incrementStats(gotoDO.getGid(), fullShortUrl, 1, uv, uip);
        } catch (Exception ex) {
            log.error("短链接统计保存失败", ex);
        }
    }
}
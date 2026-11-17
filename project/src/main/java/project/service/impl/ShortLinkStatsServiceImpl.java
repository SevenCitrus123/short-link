package project.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.dao.mapper.LinkAccessLogsMapper;
import project.dao.mapper.LinkBrowserStatsMapper;
import project.dao.mapper.LinkLocaleStatsMapper;
import project.dao.mapper.LinkOsStatsMapper;
import project.dto.req.ShortLinkStatsReqDTO;
import project.dto.resp.ShortLinkStatsBrowserRespDTO;
import project.dto.resp.ShortLinkStatsLocaleRespDTO;
import project.dto.resp.ShortLinkStatsOsRespDTO;
import project.dto.resp.ShortLinkStatsRespDTO;
import project.dto.resp.ShortLinkStatsTopIpRespDTO;
import project.service.ShortLinkStatsService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return ShortLinkStatsRespDTO.builder()
                .topIpStats(buildTopIpStats(requestParam))
                .osStats(buildOsStats(requestParam))
                .browserStats(buildBrowserStats(requestParam))
                .localeStats(buildLocaleStats(requestParam))
                .build();
    }

    private List<ShortLinkStatsTopIpRespDTO> buildTopIpStats(ShortLinkStatsReqDTO requestParam) {
        return linkAccessLogsMapper.listTopIpByShortLink(requestParam).stream()
                .map(each -> ShortLinkStatsTopIpRespDTO.builder()
                        .ip(String.valueOf(each.get("ip")))
                        .cnt(toInt(each.get("count")))
                        .build())
                .toList();
    }

    private List<ShortLinkStatsOsRespDTO> buildOsStats(ShortLinkStatsReqDTO requestParam) {
        List<Map<String, Object>> statsList = linkOsStatsMapper.listOsStatsByShortLink(requestParam);
        int sum = statsList.stream().mapToInt(each -> toInt(each.get("count"))).sum();
        return statsList.stream()
                .map(each -> ShortLinkStatsOsRespDTO.builder()
                        .os(String.valueOf(each.get("os")))
                        .cnt(toInt(each.get("count")))
                        .ratio(ratio(toInt(each.get("count")), sum))
                        .build())
                .toList();
    }

    private List<ShortLinkStatsBrowserRespDTO> buildBrowserStats(ShortLinkStatsReqDTO requestParam) {
        List<Map<String, Object>> statsList = linkBrowserStatsMapper.listBrowserStatsByShortLink(requestParam);
        int sum = statsList.stream().mapToInt(each -> toInt(each.get("count"))).sum();
        return statsList.stream()
                .map(each -> ShortLinkStatsBrowserRespDTO.builder()
                        .browser(String.valueOf(each.get("browser")))
                        .cnt(toInt(each.get("count")))
                        .ratio(ratio(toInt(each.get("count")), sum))
                        .build())
                .toList();
    }

    private List<ShortLinkStatsLocaleRespDTO> buildLocaleStats(ShortLinkStatsReqDTO requestParam) {
        List<Map<String, Object>> statsList = linkLocaleStatsMapper.listLocaleStatsByShortLink(requestParam);
        int sum = statsList.stream().mapToInt(each -> toInt(each.get("count"))).sum();
        return statsList.stream()
                .map(each -> ShortLinkStatsLocaleRespDTO.builder()
                        .locale(String.valueOf(each.get("province")))
                        .cnt(toInt(each.get("count")))
                        .ratio(ratio(toInt(each.get("count")), sum))
                        .build())
                .toList();
    }

    private int toInt(Object value) {
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    private double ratio(int cnt, int sum) {
        if (sum == 0) {
            return 0D;
        }
        return Math.round((double) cnt / sum * 100.0) / 100.0;
    }
}

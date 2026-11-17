package project.service;

import project.dto.req.ShortLinkStatsReqDTO;
import project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService {

    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);
}

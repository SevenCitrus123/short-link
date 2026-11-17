package project.service;

import project.dto.biz.ShortLinkStatsRecordDTO;

public interface ShortLinkStatsSaveService {

    void saveStats(ShortLinkStatsRecordDTO statsRecord);
}
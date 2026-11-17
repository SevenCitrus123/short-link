package project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsRespDTO {

    private List<ShortLinkStatsTopIpRespDTO> topIpStats;

    private List<ShortLinkStatsOsRespDTO> osStats;

    private List<ShortLinkStatsBrowserRespDTO> browserStats;

    private List<ShortLinkStatsLocaleRespDTO> localeStats;
}

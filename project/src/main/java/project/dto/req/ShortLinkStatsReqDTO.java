package project.dto.req;

import lombok.Data;

@Data
public class ShortLinkStatsReqDTO {

    private String fullShortUrl;

    private String startDate;

    private String endDate;
}

package project.dto.biz;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ShortLinkStatsRecordDTO {

    private String fullShortUrl;

    private String uv;

    private Boolean uvFirstFlag;

    private Boolean uipFirstFlag;

    private String remoteAddr;

    private String os;

    private String browser;

    private String device;

    private String network;

    private Date currentDate;
}
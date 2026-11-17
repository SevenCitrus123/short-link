package admin.remote.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class ShortLinkStatsRespDTO {

    private List<TopIp> topIpStats;

    private List<Os> osStats;

    private List<Browser> browserStats;

    private List<Locale> localeStats;

    @Data
    public static class TopIp {

        private String ip;

        private Integer cnt;
    }

    @Data
    public static class Os {

        private String os;

        private Integer cnt;

        private Double ratio;
    }

    @Data
    public static class Browser {

        private String browser;

        private Integer cnt;

        private Double ratio;
    }

    @Data
    public static class Locale {

        private String locale;

        private Integer cnt;

        private Double ratio;
    }
}

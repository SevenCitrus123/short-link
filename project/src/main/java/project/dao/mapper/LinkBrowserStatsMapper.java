package project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import project.dao.entity.LinkBrowserStatsDO;
import project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;
import java.util.Map;

public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {

    @Insert("INSERT INTO t_link_browser_stats (full_short_url, date, cnt, browser, create_time, update_time, del_flag) " +
            "VALUES (#{s.fullShortUrl}, #{s.date}, #{s.cnt}, #{s.browser}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{s.cnt}")
    void shortLinkBrowserState(@Param("s") LinkBrowserStatsDO linkBrowserStatsDO);

    @Select("SELECT browser, SUM(cnt) AS count FROM t_link_browser_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} " +
            "AND date BETWEEN #{param.startDate} AND #{param.endDate} " +
            "GROUP BY browser")
    List<Map<String, Object>> listBrowserStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}

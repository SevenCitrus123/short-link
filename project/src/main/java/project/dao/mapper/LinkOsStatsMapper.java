package project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import project.dao.entity.LinkOsStatsDO;
import project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;
import java.util.Map;

public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    @Insert("INSERT INTO t_link_os_stats (full_short_url, date, cnt, os, create_time, update_time, del_flag) " +
            "VALUES (#{s.fullShortUrl}, #{s.date}, #{s.cnt}, #{s.os}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{s.cnt}")
    void shortLinkOsState(@Param("s") LinkOsStatsDO linkOsStatsDO);

    @Select("SELECT os, SUM(cnt) AS count FROM t_link_os_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} " +
            "AND date BETWEEN #{param.startDate} AND #{param.endDate} " +
            "GROUP BY os")
    List<Map<String, Object>> listOsStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}

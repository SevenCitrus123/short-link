package project.dao.mapper;

import project.dao.entity.LinkAccessStatsDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    @Insert("INSERT INTO t_link_access_stats (full_short_url, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag) " +
            "VALUES (#{s.fullShortUrl}, #{s.date}, #{s.pv}, #{s.uv}, #{s.uip}, #{s.hour}, #{s.weekday}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE pv = pv + #{s.pv}, uv = uv + #{s.uv}, uip = uip + #{s.uip}")
    void shortLinkStats(@Param("s") LinkAccessStatsDO linkAccessStatsDO);
}
package project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import project.dao.entity.LinkStatsTodayDO;

public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    @Insert("INSERT INTO t_link_stats_today (full_short_url, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag) " +
            "VALUES (#{s.fullShortUrl}, #{s.date}, #{s.todayPv}, #{s.todayUv}, #{s.todayUip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE today_pv = today_pv + #{s.todayPv}, today_uv = today_uv + #{s.todayUv}, today_uip = today_uip + #{s.todayUip}")
    void shortLinkTodayState(@Param("s") LinkStatsTodayDO linkStatsTodayDO);
}
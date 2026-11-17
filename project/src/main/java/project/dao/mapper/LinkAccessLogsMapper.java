package project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import project.dao.entity.LinkAccessLogsDO;
import project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;
import java.util.Map;

public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    @Select("SELECT ip, COUNT(ip) AS count FROM t_link_access_logs " +
            "WHERE full_short_url = #{param.fullShortUrl} " +
            "AND create_time BETWEEN #{param.startDate} AND #{param.endDate} " +
            "GROUP BY ip ORDER BY count DESC LIMIT 5")
    List<Map<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}

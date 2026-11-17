package project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import project.dao.entity.ShortLinkDO;

public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {
    @Update("UPDATE t_link SET total_pv = total_pv + #{pv}, total_uv = total_uv + #{uv}, total_uip = total_uip + #{uip} " +
            "WHERE gid = #{gid} AND full_short_url = #{fullShortUrl}")
    int incrementStats(@Param("gid") String gid,
                       @Param("fullShortUrl") String fullShortUrl,
                       @Param("pv") Integer pv,
                       @Param("uv") Integer uv,
                       @Param("uip") Integer uip);
}

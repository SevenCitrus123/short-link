package project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_stats_today")
public class LinkStatsTodayDO {

    private Long id;

    private String fullShortUrl;

    private Date date;

    private Integer todayPv;

    private Integer todayUv;

    private Integer todayUip;
}
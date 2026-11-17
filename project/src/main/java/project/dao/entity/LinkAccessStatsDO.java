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
@TableName("t_link_access_stats")
public class LinkAccessStatsDO {

    private Long id;

    private String fullShortUrl;

    private Date date;

    private Integer pv;

    private Integer uv;

    private Integer uip;

    private Integer hour;

    private Integer weekday;
}
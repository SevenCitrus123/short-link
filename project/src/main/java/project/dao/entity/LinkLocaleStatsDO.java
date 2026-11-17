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
@TableName("t_link_locale_stats")
public class LinkLocaleStatsDO {

    private Long id;

    private String fullShortUrl;

    private Date date;

    private Integer cnt;

    private String country;

    private String province;

    private String city;

    private String adcode;
}

package project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_access_logs")
public class LinkAccessLogsDO {

    private Long id;

    private String fullShortUrl;

    private String user;

    private String ip;

    private String browser;

    private String os;

    private String network;

    private String device;

    private String locale;
}
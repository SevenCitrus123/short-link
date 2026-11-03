package admin.dto.resp.test.java.admin.test;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ShardingDBCreator {
    public static void main(String[] args) {
        String SQL =
        "create table t_link_%d\n" +
                "(\n" +
                "    id              bigint auto_increment comment 'ID'\n" +
                "        primary key,\n" +
                "    domain          varchar(128)                  null comment '域名',\n" +
                "    short_uri       varchar(8) collate utf8_bin   null comment '短链接',\n" +
                "    full_short_url  varchar(128)                  null comment '完整短链接',\n" +
                "    origin_url      varchar(1024)                 null comment '原始链接',\n" +
                "    click_num       int         default 0         null comment '点击量',\n" +
                "    gid             varchar(32) default 'default' null comment '分组标识',\n" +
                "    favicon         varchar(256)                  null comment '网站图标',\n" +
                "    enable_status   tinyint(1)                    null comment '启用标识 0：启用 1：未启用',\n" +
                "    created_type    tinyint(1)                    null comment '创建类型 0：接口创建 1：控制台创建',\n" +
                "    valid_date_type tinyint(1)                    null comment '有效期类型 0：永久有效 1：自定义',\n" +
                "    valid_date      datetime                      null comment '有效期',\n" +
                "    `describe`      varchar(1024)                 null comment '描述',\n" +
                "    total_pv        int                           null comment '历史PV',\n" +
                "    total_uv        int                           null comment '历史UV',\n" +
                "    total_uip       int                           null comment '历史UIP',\n" +
                "    create_time     datetime                      null comment '创建时间',\n" +
                "    update_time     datetime                      null comment '修改时间',\n" +
                "    del_time        bigint      default 0         null comment '删除时间戳',\n" +
                "    del_flag        tinyint(1)                    null comment '删除标识 0：未删除 1：已删除',\n" +
                "    constraint `idx_unique_full-short-url`\n" +
                "        unique (full_short_url, del_time)\n" +
                ")\n" +
                "    charset = utf8mb4;\n" +
                "\n";
        for (int i = 0; i < 16; i++) {
            System.out.printf(SQL + "%n", i);
        }
    }

}

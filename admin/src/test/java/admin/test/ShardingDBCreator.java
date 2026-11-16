package admin.test;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ShardingDBCreator {
    public static void main(String[] args) {
        String SQL =
        "create table t_link_goto_%d\n" +
                "(\n" +
                "    id             bigint auto_increment comment 'ID'\n" +
                "        primary key,\n" +
                "    gid            varchar(32) default 'default' null comment '分组标识',\n" +
                "    full_short_url varchar(128)                  null comment '完整短链接',\n" +
                "    constraint idx_full_short_url\n" +
                "        unique (full_short_url)\n" +
                ")\n" +
                "    charset = utf8mb4;\n" +
                "\n";
        for (int i = 0; i < 16; i++) {
            System.out.printf(SQL + "%n", i);
        }
    }

}

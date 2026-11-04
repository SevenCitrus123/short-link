package admin.dto.resp.test.java.admin.test;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ShardingDBCreator {
    public static void main(String[] args) {
        String SQL =
                "create table t_group_%d\n" +
                        "(\n" +
                        "    id          bigint auto_increment comment 'ID'\n" +
                        "        primary key,\n" +
                        "    gid         varchar(32)  null comment '分组标识',\n" +
                        "    name        varchar(64)  null comment '分组名称',\n" +
                        "    username    varchar(256) null comment '创建分组用户名',\n" +
                        "    sort_order  int(3)       null comment '分组排序',\n" +
                        "    create_time datetime     null comment '创建时间',\n" +
                        "    update_time datetime     null comment '修改时间',\n" +
                        "    del_flag    tinyint(1)   null comment '删除标识 0：未删除 1：已删除',\n" +
                        "    constraint uk_gid\n" +
                        "        unique (gid)\n" +
                        ")\n" +
                        "    charset = utf8mb4;\n" +
                        "\n" +
                        "create index idx_username\n" +
                        "    on t_group_%d (username);\n" +
                        "\n";
        for (int i = 0; i < 16; i++) {
            System.out.printf(SQL, i, i);
        }
    }

}

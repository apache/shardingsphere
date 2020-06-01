+++
title = "Seata 事务"
weight = 5
+++

## 启动 Seata 服务

按照 [seata-work-shop](https://github.com/seata/seata-workshop)中的步骤，下载并启动 Seata 服务器。

## 创建日志表

在每一个分片数据库实例中执创建 `undo_log`表（以 MySQL 为例）。

```sql
CREATE TABLE IF NOT EXISTS `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'increment id',
  `branch_id`     BIGINT(20)   NOT NULL COMMENT 'branch transaction id',
  `xid`           VARCHAR(100) NOT NULL COMMENT 'global transaction id',
  `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
  `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created`   DATETIME     NOT NULL COMMENT 'create datetime',
  `log_modified`  DATETIME     NOT NULL COMMENT 'modify datetime',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COMMENT ='AT transaction mode undo table';
```

## 修改配置

在 classpath 中增加 `seata.conf` 文件。

```conf
client {
    application.id = example    ## 应用唯一主键
    transaction.service.group = my_test_tx_group   ## 所属事务组
}
```

根据实际场景修改 Seata 的 `file.conf`和 `registry.conf` 文件。

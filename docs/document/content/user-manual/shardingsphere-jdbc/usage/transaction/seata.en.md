+++
title = "Seata Transaction"
weight = 5
+++

## Startup Seata Server

Download seata server according to [seata-work-shop](https://github.com/seata/seata-workshop).

## Create Undo Log Table

Create `undo_log` table in each physical database (sample for MySQL).

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

## Update Configuration

Configure `seata.conf` file in classpath.

```conf
client {
    application.id = example   ## application unique ID
    transaction.service.group = my_test_tx_group   ## transaction group
}
```

Modify `file.conf` and `registry.conf` if needed.

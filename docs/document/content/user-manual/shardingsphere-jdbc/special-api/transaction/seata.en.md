+++
title = "Seata Transaction"
weight = 7
+++

## Background

Apache ShardingSphere provides BASE transactions that integrate the Seata implementation.

## Procedure

1. Start Seata Server
2. Create the log table
3. Add the Seata configuration

## Sample

### Start Seata Server

Refer to [seata-work-shop](https://github.com/seata/seata-workshop) to download and start the Seata server.

### Create undo_log table

Create the `undo_log` table in each shard database instance (take MySQL as an example).

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

### Modify configuration

Add the `seata.conf` file to the classpath.

```conf
client {
    application.id = example    ## Apply the only primary key
    transaction.service.group = my_test_tx_group   ## The transaction group it belongs to.
}
```

Modify the `file.conf` and `registry.conf` files of Seata as required.

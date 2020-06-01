+++
title = "Distributed Transaction"
weight = 15
+++


## Other configuration for Sharding Transaction Manager

### XA Transaction (Optional)

Default ShardingSphere XA transaction manager is Atomikos. `xa_tx.log` generated in the project log is necessary for the recovery when XA crashes. Please do not delete it.

Or you can add `jta.properties` in `classpath` of the program to customize Atomikos configurations. 
For detailed configuration rules, please refer to the [official documentation](https://www.atomikos.com/Documentation/JtaProperties) of Atomikos.

### BASE Transaction (SEATA-AT)

1.Download seata server according to [seata-work-shop](https://github.com/seata/seata-workshop), refer to step6 and step7 is OK.

2.Create `undo_log` table in each physical database.(only mysql supported before seata version 0.8.X)

```conf
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

3.Config `seata.conf` file in classpath.

```conf
client {
    application.id = example   ## application unique id.
    transaction.service.group = my_test_tx_group   ## transaction group
}

```

4.Modify file.conf and registry.conf if you need.

## Distributed Transaction Example

* [official example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example/sharding-example)
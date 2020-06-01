+++
title = "分布式事务"
weight = 15
+++


## 分布式事务管理器的特有配置

### XA事务管理器参数配置（可选）

ShardingSphere默认的XA事务管理器为Atomikos，在项目的logs目录中会生成`xa_tx.log`, 这是XA崩溃恢复时所需的日志，请勿删除。

也可以通过在项目的classpath中添加`jta.properties`来定制化Atomikos配置项。具体的配置规则请参考Atomikos的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

### BASE柔性事务管理器（SEATA-AT配置）

1.按照[seata-work-shop](https://github.com/seata/seata-workshop)中的步骤，下载并启动seata server，参考 Step6 和 Step7即可。

2.在每一个分片数据库实例中执创建undo_log表（以MySQL为例）

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
3.在classpath中增加seata.conf

```conf
client {
    application.id = example    ## 应用唯一id
    transaction.service.group = my_test_tx_group   ## 所属事务组
}
```

4.根据实际场景修改seata的file.conf和registry.conf文件

## 分布式事务example

* [官方example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example/sharding-example)

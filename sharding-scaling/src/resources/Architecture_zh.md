# 应用架构

## 应用架构图
![应用架构图](https://github.com/apache/incubator-shardingsphere/blob/sharding-scaling/sharding-scaling/src/resources/ControllerProcess.png)

说明：
1. Job为用户提交的迁移任务，在sharding-scaling一个job会被划分成多个tasks，真正的数据迁移任务由task来执行，`ScalingJobController`类负责Job生命周期的管理，`SyncTaskController`负责tasks生命周期的管理。
2. `Reader`负责从源端读取数据，`Channel`负责数据的传输，`Writer`负责从`Channel`读取数据然后写入目标端。

## 迁移方案设计
全量方案：基于JDBC接口遍历数据，具有实现简单、兼容性好的优点。

增量方案：

- MySQL：伪装成从机，读取解析源端binlog日志来进行增量数据同步。
- PostgreSQL：采用官方[test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html)来进行增量数据同步。

## 路线图
![应用路线图](https://github.com/apache/incubator-shardingsphere/blob/sharding-scaling/sharding-scaling/src/resources/roadmap.png)

上述是我们的规划路线图，后续sharding-scaling模块将会更加便捷地帮助用户进行数据的扩缩容操作，这些规划包括自动切换配置、断点续传以及数据正确性对比等。而且所有的这些操作都可以通过UI界面来进行：

![应用流程图](https://github.com/apache/incubator-shardingsphere/blob/sharding-scaling/sharding-scaling/src/resources/workflow.png)

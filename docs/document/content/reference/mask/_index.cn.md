+++
pre = "<b>7.8. </b>"
title = "数据脱敏"
weight = 8
+++

## 处理流程详解

Apache ShardingSphere 通过对用户查询的 SQL 进行解析，并依据用户提供的脱敏规则对 SQL 执行结果进行装饰，从而实现对原文数据进行脱敏。
### 整体架构

![1](https://shardingsphere.apache.org/document/current/img/mask/1_cn.png)

脱敏模块将用户发起的 SQL 进行拦截，并通过 SQL 语法解析器进行解析、执行，再依据用户传入的脱敏规则，找出需要脱敏的字段和所使用的脱敏算法对查询结果进行装饰，返回给客户端。


### 脱敏规则

在详解整套流程之前，我们需要先了解下脱敏规则与配置，这是认识整套流程的基础。脱敏配置主要分为三部分：数据源配置，脱敏算法配置，脱敏表配置：

![2](https://shardingsphere.apache.org/document/current/img/mask/2_cn.png)

**数据源配置**：指数据源配置。

**脱敏算法配置**：指使用什么脱敏算法。目前 ShardingSphere 内置了多种脱敏算法：MD5、KEEP_FIRST_N_LAST_M、KEEP_FROM_X_TO_Y 、MASK_FIRST_N_LAST_M、MASK_FROM_X_TO_Y、MASK_BEFORE_SPECIAL_CHARS、MASK_AFTER_SPECIAL_CHARS 和 GENERIC_TABLE_RANDOM_REPLACE。用户还可以通过实现 ShardingSphere 提供的接口，自行实现一套脱敏算法。

**脱敏表配置**：用于告诉 ShardingSphere 数据表里哪个列用于数据脱敏、使用什么算法脱敏。

**脱敏规则创建即可生效**

### 脱敏处理过程

举例说明，假如数据库里有一张表叫做 `t_user`，这张表里有一个字段 `phone_number` 使用 `MASK_FROM_X_TO_Y`  进行算法处理，Apache ShardingSphere 不会改变数据存储，只会按脱敏算法对结果进行装饰，从而达到脱敏的效果

如下图所示：

![3](https://shardingsphere.apache.org/document/current/img/mask/3_cn.png)



+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "详细功能列表"
weight = 3
prev = "/01-start/faq/"
next = "/01-start/limitations/"

+++

## 功能性需求

### 分库分表
1. 分片规则自由定制
2. 支持多分片键
3. 支持通过=，BETWEEN，IN分片
4. 支持级联表
5. 支持多表笛卡尔积查询
6. 支持多表结果归并
7. 支持聚合查询结果归并
8. 支持AVG函数改写为SUM/COUNT
9. 支持ORDER BY结果归并
10. 支持GROUP BY结果归并
11. 支持LIMIT分页查询以及多库表结果改写及归并

### 柔性事务
1. 最大努力送达型
2. TCC型(TBD)

## 易用性

1. JDBC协议适配
    
    1.1. 全ORM支持
    
    1.2. 全数据库连接池支持
    
    1.3. 多数据库支持(目前仅MySQL，未来计划支持Oracle，SQLServer和DB2)
2. 配置多样性

    2.1. Spring命名空间支持
    
    2.2. YAML命名空间支持
    
    2.3. 基于动态语言的分片策略配置
3. Metrics统计监控

## 性能
1. 基于Druid的高性能SQL解析
2. 多线程处理结果归并
3. 性能损失率约0.02%

## 稳定性
完善的疲劳测试，普通查询无Full GC（GROUP BY除外）

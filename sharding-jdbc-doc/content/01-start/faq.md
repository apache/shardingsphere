+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "FAQ"
weight = 2
prev = "/01-start/quick-start"
next = "/01-start/features"

+++

## 使用问题

### 使用Spring命名空间时在网上相应地址找不到xsd?

Spring命名空间使用规范并未强制要求将xsd文件部署至公网地址，因此我们并未将http://www.dangdang.com/schema/ddframe/rdb/rdb.xsd部署至公网，不影响正常使用。

sharding-jdbc-config-spring的jar包中`META-INF\spring.schemas`配置了xsd文件的位置：`http\://www.dangdang.com/schema/ddframe/rdb/rdb.xsd=META-INF/namespace/rdb.xsd`，需确保jar包中该文件存在。

### 异常：Cloud not resolve placeholder ... in string value ...?

由于inline表达式内使用Groovy语法，Groovy语法的变量占位符为${},与Spring的Property占位符冲突。
故需要在Spring的配置文件中增加
<context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true"/>

### inline表达式返回结果出现浮点数？

Java的整数相除结果是整数，但是对于inline表达式中的Groovy语法则不同，整数相除结果是浮点数。
想获得除法整数结果需要将A/B改为A.intdiv(B)

## 编译源代码问题

### 阅读源码时为什么会出现编译错误?

代码使用[Lombok](https://projectlombok.org/download.html)实现极简代码。关于更多使用和安装细节，请参考官网。
+++
date = "2016-11-23T20:30:55+08:00"
title = "FAQ"
weight = 0
+++

## 阅读源码时为什么会出现编译错误?

代码使用[Lombok](https://projectlombok.org/download.html)实现极简代码。关于更多使用和安装细节，请参考官网。

## 使用Spring命名空间时在网上相应地址找不到xsd?

Spring命名空间使用规范并未强制要求将xsd文件部署至公网地址，只需在jar包的`META-INF\spring.schemas`配置，并在jar包中相关位置存在即可。

我们并未将`http://www.dangdang.com/schema/ddframe/rdb/rdb.xsd`部署至公网，但并不影响使用。相关问题请参考Spring命名空间规范。

## 异常：Cloud not resolve placeholder ... in string value ...?

由于inline表达式内使用Groovy语法，Groovy语法的变量占位符为`${}`,与Spring的Property占位符冲突。
故需要在Spring的配置文件中增加<context:property-placeholder location="classpath:conf/rdb/conf.properties" **ignore-unresolvable="true"**/>

## inline表达式返回结果出现浮点数？

Java的整数相除结果是整数，但是对于inline表达式中的Groovy语法则不同，整数相除结果是浮点数。
想获得除法整数结果需要将`A/B`改为`A.intdiv(B)`
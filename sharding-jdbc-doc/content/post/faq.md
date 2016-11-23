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
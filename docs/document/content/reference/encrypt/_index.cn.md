+++
pre = "<b>7.7. </b>"
title = "数据加密"
weight = 7
+++

## 处理流程详解

Apache ShardingSphere 通过对用户输入的 SQL 进行解析，并依据用户提供的加密规则对 SQL 进行改写，从而实现对原文数据进行加密。
在用户查询数据时，它仅从数据库中取出密文数据，并对其解密，最终将解密后的原始数据返回给用户。
Apache ShardingSphere 自动化 & 透明化了数据加密过程，让用户无需关注数据加密的实现细节，像使用普通数据那样使用加密数据。

### 整体架构

![1](https://shardingsphere.apache.org/document/current/img/encrypt/1_cn_v2.png)

加密模块将用户发起的 SQL 进行拦截，并通过 SQL 语法解析器进行解析、理解 SQL 行为，再依据用户传入的加密规则，找出需要加密的字段和所使用的加解密算法对目标字段进行加解密处理后，再与底层数据库进行交互。
Apache ShardingSphere 会将用户请求的明文进行加密后存储到底层数据库；并在用户查询时，将密文从数据库中取出进行解密后返回给终端用户。
通过屏蔽对数据的加密处理，使用户无需感知解析 SQL、数据加密、数据解密的处理过程，就像在使用普通数据一样使用加密数据。

### 加密规则

在详解整套流程之前，我们需要先了解下加密规则与配置，这是认识整套流程的基础。加密配置主要分为三部分：数据源配置，加密算法配置，加密表配置，其详情如下图所示：

![2](https://shardingsphere.apache.org/document/current/img/encrypt/2_cn_v3.png)

**数据源配置**：指数据源配置。

**加密算法配置**：指使用什么加密算法进行加解密。目前 ShardingSphere 内置了三种加解密算法：AES，MD5 和 RC4。用户还可以通过实现 ShardingSphere 提供的接口，自行实现一套加解密算法。

**加密表配置**：用于告诉 ShardingSphere 数据表里哪个列用于存储密文数据（cipherColumn）、使用什么算法加解密（encryptorName）、哪个列用于存储辅助查询数据（assistedQueryColumn）、使用什么算法加解密（assistedQueryEncryptorName）以及用户想使用哪个列进行 SQL 编写（logicColumn）。

>  如何理解 `用户想使用哪个列进行 SQL 编写（logicColumn）`？
>
> 我们可以从加密模块存在的意义来理解。加密模块最终目的是希望屏蔽底层对数据的加密处理，也就是说我们不希望用户知道数据是如何被加解密的、如何将密文数据存储到 cipherColumn，将辅助查询数据存储到 assistedQueryColumn。
换句话说，我们不希望用户知道 cipherColumn 和 assistedQueryColumn 的存在和使用。
所以，我们需要给用户提供一个概念意义上的列，这个列可以脱离底层数据库的真实列，它可以是数据库表里的一个真实列，也可以不是，从而使得用户可以随意改变底层数据库的 cipherColumn 和 assistedQueryColumn 的列名。
只要用户的 SQL 面向这个逻辑列进行编写，并在加密规则里给出 logicColumn、cipherColumn、assistedQueryColumn 之间正确的映射关系即可。
>
> 为什么要这么做呢？答案在文章后面，即为了让已上线的业务能无缝、透明、安全地进行数据加密迁移。

### 加密处理过程

举例说明，假如数据库里有一张表叫做 `t_user`，这张表里实际有两个字段 `pwd_cipher`，用于存放密文数据、`pwd_assisted_query`，用于存放辅助查询数据，同时定义 logicColumn 为 `pwd`。
那么，用户在编写 SQL 时应该面向 logicColumn 进行编写，即 `INSERT INTO t_user SET pwd = '123'`。
Apache ShardingSphere 接收到该 SQL，通过用户提供的加密配置，发现 `pwd` 是 logicColumn，于是便对逻辑列及其对应的明文数据进行加密处理。
**Apache ShardingSphere 将面向用户的逻辑列与面向底层数据库的密文列进行了列名以及数据的加密映射转换。** 
如下图所示：

![3](https://shardingsphere.apache.org/document/current/img/encrypt/3_cn_v2.png)

即依据用户提供的加密规则，将用户 SQL 与底层数据表结构割裂开来，使得用户的 SQL 编写不再依赖于真实的数据库表结构。
而用户与底层数据库之间的衔接、映射、转换交由 Apache ShardingSphere 进行处理。

下方图片展示了使用加密模块进行增删改查时，其中的处理流程和转换逻辑，如下图所示。

![4](https://shardingsphere.apache.org/document/current/img/encrypt/4_cn_v3.png)

## 解决方案详解

在了解了 Apache ShardingSphere 加密处理流程后，即可将加密配置、加密处理流程与实际场景进行结合。
所有的设计开发都是为了解决业务场景遇到的痛点。那么面对之前提到的业务场景需求，又应该如何使用 Apache ShardingSphere 这把利器来满足业务需求呢？

业务场景分析：新上线业务由于一切从零开始，不存在历史数据清洗问题，所以相对简单。

解决方案说明：选择合适的加密算法，如 AES 后，只需配置逻辑列（面向用户编写 SQL ）和密文列（数据表存密文数据）即可，**逻辑列和密文列可以相同也可以不同**。建议配置如下（YAML 格式展示）：

```yaml
-!ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd:
          cipher:
            name: pwd_cipher
            encryptorName: aes_encryptor
          assistedQuery:
            name: pwd_assisted_query
            encryptorName: pwd_assisted_query_cipher
```

使用这套配置， Apache ShardingSphere 只需将 logicColumn 和 cipherColumn，assistedQueryColumn 进行转换，底层数据表不存储明文，只存储了密文，这也是安全审计部分的要求所在。
整体处理流程如下图所示：

![5](https://shardingsphere.apache.org/document/current/img/encrypt/5_cn_v2.png)

## 中间件加密服务优势

1. 自动化 & 透明化数据加密过程，用户无需关注加密中间实现细节。
2. 提供多种内置、第三方（AKS）的加密算法，用户仅需简单配置即可使用。
3. 提供加密算法 API 接口，用户可实现接口，从而使用自定义加密算法进行数据加密。
4. 支持切换不同的加密算法。

## 加密算法解析

Apache ShardingSphere 提供了加密算法用于数据加密，即 `EncryptAlgorithm`。

一方面，Apache ShardingSphere 为用户提供了内置的加解密实现类，用户只需进行配置即可使用；
另一方面，为了满足用户不同场景的需求，我们还开放了相关加解密接口，用户可依据这两种类型的接口提供具体实现类。
再进行简单配置，即可让 Apache ShardingSphere 调用用户自定义的加解密方案进行数据加密。

### EncryptAlgorithm

该解决方案通过提供 `encrypt()`，`decrypt()` 两种方法对需要加密的数据进行加解密。
在用户进行 `INSERT`，`DELETE`，`UPDATE` 时，ShardingSphere会按照用户配置，对SQL进行解析、改写、路由，并调用 `encrypt()` 将数据加密后存储到数据库， 
而在 `SELECT` 时，则调用 `decrypt()` 方法将从数据库中取出的加密数据进行逆向解密，最终将原始数据返回给用户。

当前，Apache ShardingSphere 针对这种类型的加密解决方案提供了三种具体实现类，分别是 MD5（不可逆），AES（可逆），RC4（可逆），用户只需配置即可使用这三种内置的方案。

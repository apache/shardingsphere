+++
pre = "<b>3.3.5. </b>"
title = "Data Masking"
weight = 5
+++

## Background

Security control has always been a crucial link of orchestration; data masking falls into this category. For both Internet enterprises and traditional sectors, data security has always been a highly valued and sensitive topic. Data masking refers to transforming some sensitive information through masking rules to safely protect the private data. Data involves client's security or business sensibility, such as ID number, phone number, card number, client number and other personal information, requires data masking according to relevant regulations.

Because of that, ShardingSphere has provided data masking, which stores users' sensitive information in the database after encryption. When users search for them, the information will be decrypted and returned to users in the original form. 

ShardingSphere has made the encryption and decryption processes totally transparent to users, who can store desensitized data and acquire original data without any awareness. In addition, ShardingSphere has provided internal masking algorithms, which can be directly used by users. In the same time, we have also provided masking algorithm related interfaces, which can be implemented by users themselves. After simple configurations, ShardingSphere can use algorithms provided by users to perform encryption, decryption and masking.

## Preface

Apache ShardingSphere is an ecosystem of open source distributed database middleware solutions. It consists of Sharding-JDBC, Sharding-Proxy, and Sharding-Sidecar (in planning) which are independent of each other, but can be used in mixed deployment. All of these can provide standardized data sharding, distributed transactions, and distributed governance functions, and can be applied to various situation such as Java homogeneous, heterogeneous languages, containers, cloud native, and so on.

The data decryption module belongs to the sub-function module under the core function of ShardingSphere distributed governance. It parses the SQL input by the user and rewrites the SQL according to the decryption configuration provided by the user, thereby encrypting the original data and storing the original data and store the original data (optional) and cipher data to database at the same time. When the user queries the data, it takes the cipher data from the database and decrypts it, and finally returns the decrypted original data to the user. Apache ShardingSphere distributed database middleware automates and transparentizes the process of data desensitization, so that users do not need to pay attention to the details of data decryption and use decrypted data like ordinary data.  In addition, ShardingSphere can provide a relatively complete set of solutions for the encryption of online services or the encryption function of new services.

## 需求场景分析

对于数据脱敏的需求，在现实的业务场景中一般分为两种情况：

1. 新业务上线，安全部门规定需将涉及用户敏感信息，例如银行、手机号码等进行加密后存储到数据库，在使用的时候再进行解密处理。因为是全新系统，因而没有存量数据清洗问题，所以实现相对简单。

2. 已上线业务，之前一直将明文存储在数据库中。相关部门突然需要对已上线业务进行脱敏整改。这种场景一般需要处理三个问题：

   a) 历史数据需要如何进行脱敏处理，即洗数。

   b) 如何能在不改动业务SQL和逻辑情况下，将新增数据进行脱敏处理，并存储到数据库；在使用时，再进行解密取出。

   c) 如何较为安全、无缝、透明化地实现业务系统在明文与密文数据间的迁移。

## 处理流程详解

### 整体架构

ShardingSphere提供的Encrypt-JDBC和业务代码部署在一起。业务方需面向Encrypt-JDBC进行JDBC编程。由于Encrypt-JDBC实现所有JDBC标准接口，业务代码无需做额外改造即可兼容使用。此时，业务代码所有与数据库的交互行为交由Encrypt-JDBC负责。业务只需提供脱敏规则即可。**作为业务代码与底层数据库中间的桥梁，Encrypt-JDBC便可拦截用户行为，并在改造行为后与数据库交互。**

![1](https://shardingsphere.apache.org/document/current/img/encrypt/1.png)

Encrypt-JDBC将用户发起的SQL进行拦截，并通过SQL语法解析器进行解析、理解SQL行为，再依据用户传入的脱敏规则，找出需要脱敏的字段和所使用的加解密器对目标字段进行加解密处理后，再与底层数据库进行交互。ShardingSphere会将用户请求的明文进行加密后存储到底层数据库；并在用户查询时，将密文从数据库中取出进行解密后返回给终端用户。ShardingSphere通过屏蔽对数据的脱敏处理，使用户无需感知解析SQL、数据加密、数据解密的处理过程，就像在使用普通数据一样使用脱敏数据。

### 脱敏规则

在详解整套流程之前，我们需要先了解下脱敏规则与配置，这是认识整套流程的基础。脱敏配置主要分为四部分：数据源配置，加密器配置，脱敏表配置以及查询属性配置，其详情如下图所示：

![2](https://shardingsphere.apache.org/document/current/img/encrypt/2.png)

**数据源配置**：是指DataSource的配置。

**加密器配置**：是指使用什么加密策略进行加解密。目前ShardingSphere内置了两种加解密策略：AES/MD5。用户还可以通过实现ShardingSphere提供的接口，自行实现一套加解密算法。

**脱敏表配置**：用于告诉ShardingSphere数据表里哪个列用于存储密文数据（cipherColumn）、哪个列用于存储明文数据（plainColumn）以及用户想使用哪个列进行SQL编写（logicColumn）。

>  如何理解`用户想使用哪个列进行SQL编写（logicColumn）`？
>
> 我们可以从Encrypt-JDBC存在的意义来理解。Encrypt-JDBC最终目的是希望屏蔽底层对数据的脱敏处理，也就是说我们不希望用户知道数据是如何被加解密的、如何将明文数据存储到plainColumn，将密文数据存储到cipherColumn。换句话说，我们不希望用户知道plainColumn和cipherColumn的存在和使用。所以，我们需要给用户提供一个概念意义上的列，这个列可以脱离底层数据库的真实列，它可以是数据库表里的一个真实列，也可以不是，从而使得用户可以随意改变底层数据库的plainColumn和cipherColumn的列名。或者删除plainColumn，选择永远不再存储明文，只存储密文。只要用户的SQL面向这个逻辑列进行编写，并在脱敏规则里给出logicColumn和plainColumn、cipherColumn之间正确的映射关系即可。
>
> 为什么要这么做呢？答案在文章后面，即为了让已上线的业务能无缝、透明、安全地进行数据脱敏迁移。

**查询属性的配置**：当底层数据库表里同时存储了明文数据、密文数据后，该属性开关用于决定是直接查询数据库表里的明文数据进行返回，还是查询密文数据通过Encrypt-JDBC解密后返回。

### 脱敏处理过程

举个栗子，假如数据库里有一张表叫做t_user，这张表里实际有两个字段pwd_plain，用于存放明文数据、pwd_cipher，用于存放密文数据，同时定义logicColumn为pwd。那么，用户在编写SQL时应该面向logicColumn进行编写，即INSERT INTO t_user SET pwd = '123'。ShardingSphere接收到该SQL，通过用户提供的脱敏配置，发现pwd是logicColumn，于是便对逻辑列及其对应的明文数据进行脱敏处理。可以看出**ShardingSphere将面向用户的逻辑列与面向底层数据库的明文列和密文列进行了列名以及数据的脱敏映射转换。**如下图所示：

![3](https://shardingsphere.apache.org/document/current/img/encrypt/3.png)

**这也正是Encrypt-JDBC核心意义所在，即依据用户提供的脱敏规则，将用户SQL与底层数据表结构割裂开来，使得用户的SQL编写不再依赖于真实的数据库表结构。而用户与底层数据库之间的衔接、映射、转换交由ShardingSphere进行处理。**为什么我们要这么做？还是那句话：为了让已上线的业务能无缝、透明、安全地进行数据脱敏迁移。

为了让读者更清晰了解到Encrypt-JDBC的核心处理流程，下方图片展示了使用Encrypt-JDBC进行增删改查时，其中的处理流程和转换逻辑，如下图所示。

![4](https://shardingsphere.apache.org/document/current/img/encrypt/4.png)

## 解决方案详解

在了解了ShardingSphere脱敏处理流程后，即可将脱敏配置、脱敏处理流程与实际场景进行结合。所有的设计开发都是为了解决业务场景遇到的痛点。那么面对之前提到的业务场景需求，又应该如何使用ShardingSphere这把利器来满足业务需求呢？

### 新上线业务

业务场景分析：新上线业务由于一切从零开始，不存在历史数据清洗问题，所以相对简单。

解决方案说明：选择合适的加密器，如AES后，只需配置逻辑列（面向用户编写SQL）和密文列（数据表存密文数据）即可，**逻辑列和密文列可以相同也可以不同**。建议配置如下（Yaml格式展示）：

```yaml
encryptRule:
  encryptors:
    aes_encryptor:
      type: aes
      props:
        aes.key.value: 123456abc
  tables:
    t_user:
      columns:
        pwd:
          cipherColumn: pwd
          encryptor: aes_encryptor
```

使用这套配置，Encrypt-JDBC只需将logicColumn和cipherColumn进行转换，底层数据表不存储明文，只存储了密文，这也是安全审计部分的要求所在。如果用户希望将明文、密文一同存储到数据库，只需添加plainColumn配置即可。整体处理流程如下图所示：

![5](https://shardingsphere.apache.org/document/current/img/encrypt/5.png)

### 已上线业务改造

业务场景分析：由于业务已经在线上运行，数据库里必然存有大量明文历史数据。现在的问题是如何让历史数据得以加密清洗、如何让增量数据得以加密处理、如何让业务在新旧两套数据系统之间进行无缝、透明化迁移。

解决方案说明：在提供解决方案之前，我们先来头脑风暴一下：首先，既然是旧业务需要进行脱敏改造，那一定存储了非常重要且敏感的信息。这些信息含金量高且业务相对基础重要。如果搞错了，整个团队KPI就再见了。所以不可能一上来就停业务，禁止新数据写入，再找个加密器把历史数据全部加密清洗，再把之前重构的代码部署上线，使其能把存量和增量数据进行在线加密解密。如此简单粗暴的方式，按照历史经验来谈，一定凉凉。

那么另一种相对安全的做法是：重新搭建一套和生产环境一模一样的预发环境，然后通过相关迁移洗数工具把生产环境的**存量原文数据**加密后存储到预发环境，而**新增数据**则通过例如MySQL主从复制及业务方自行开发的工具加密后存储到预发环境的数据库里，再把重构后可以进行加解密的代码部署到预发环境。这样生产环境是一套**以明文为核心的查询修改**的环境；预发环境是一套**以密文为核心加解密查询修改**的环境。在对比一段时间无误后，可以夜间操作将生产流量切到预发环境中。此方案相对安全可靠，只是时间、人力、资金、成本较高，主要包括：预发环境搭建、生产代码整改、相关辅助工具开发等。除非无路可走，否则业务开发人员一般是从入门到放弃。

业务开发人员最希望的做法是：减少资金费用的承担、最好不要修改业务代码、能够安全平滑迁移系统。于是，ShardingSphere的脱敏功能模块便应用而生。可分为三步进行：

1. 系统迁移前

   假设系统需要对t_user的pwd字段进行脱敏处理，业务方使用Encrypt-JDBC来代替标准化的JDBC接口，此举基本不需要额外改造（我们还提供了SpringBoot，SpringNameSpace，Yaml等接入方式，满足不同业务方需求）。另外，提供一套脱敏配置规则，如下所示：

   ```yaml
   encryptRule:
     encryptors:
       aes_encryptor:
         type: aes
         props:
           aes.key.value: 123456abc
     tables:
       t_user:
         columns:
           pwd:
             plainColumn: pwd
             cipherColumn: pwd_cipher
             encryptor: aes_encryptor
   props:
       query.with.cipher.column: false
   ```

   依据上述脱敏规则可知，首先需要在数据库表t_user里新增一个字段叫做pwd_cipher，即cipherColumn，用于存放密文数据，同时我们把plainColumn设置为pwd，用于存放明文数据，而把logicColumn也设置为pwd。由于之前的代码SQL就是使用pwd进行编写，即面向逻辑列进行SQL编写，所以业务代码无需改动。通过Encrypt-JDBC，针对新增的数据，会把明文写到pwd列，并同时把明文进行加密存储到pwd_cipher列。此时，由于query.with.cipher.column设置为false，对业务应用来说，依旧使用pwd这一明文列进行查询存储，却在底层数据库表pwd_cipher上额外存储了新增数据的密文数据，其处理流程如下图所示：

   ![6](https://shardingsphere.apache.org/document/current/img/encrypt/6.png)

   新增数据在插入时，就通过Encrypt-JDBC加密为密文数据，并被存储到了cipherColumn。而现在就需要处理历史明文存量数据。**由于Apache ShardingSphere目前并未提供相关迁移洗数工具，此时需要业务方自行将pwd中的明文数据进行加密处理存储到pwd_cipher。**

2. 系统迁移中

   新增的数据已被Encrypt-JDBC将密文存储到密文列，明文存储到明文列；历史数据被业务方自行加密清洗后，将密文也存储到密文列。也就是说现在的数据库里即存放着明文也存放着密文，只是由于配置项中的query.with.cipher.column=false，所以密文一直没有被使用过。现在我们为了让系统能切到密文数据进行查询，需要将脱敏配置中的query.with.cipher.column设置为true。在重启系统后，我们发现系统业务一切正常，但是Encrypt-JDBC已经开始从数据库里取出密文列的数据，解密后返回给用户；而对于用户的增删改需求，则依旧会把原文数据存储到明文列，加密后密文数据存储到密文列。

   虽然现在业务系统通过将密文列的数据取出，解密后返回；但是，在存储的时候仍旧会存一份原文数据到明文列，这是为什么呢？答案是：为了能够进行系统回滚。**因为只要密文和明文永远同时存在，我们就可以通过开关项配置自由将业务查询切换到cipherColumn或plainColumn。**也就是说，如果将系统切到密文列进行查询时，发现系统报错，需要回滚。那么只需将query.with.cipher.column=false，Encrypt-JDBC将会还原，即又重新开始使用plainColumn进行查询。处理流程如下图所示：

   ![7](https://shardingsphere.apache.org/document/current/img/encrypt/7.png)

   

3. 系统迁移后

   由于安全审计部门要求，业务系统一般不可能让数据库的明文列和密文列永久同步保留，我们需要在系统稳定后将明文列数据删除。即我们需要在系统迁移后将plainColumn，即pwd进行删除。那问题来了，现在业务代码都是面向pwd进行编写SQL的，把底层数据表中的存放明文的pwd删除了，换用pwd_cipher进行解密得到原文数据，那岂不是意味着业务方需要整改所有SQL，从而不使用即将要被删除的pwd列？还记得我们Encrypt-JDBC的核心意义所在吗？

   > 这也正是Encrypt-JDBC核心意义所在，即依据用户提供的脱敏规则，将用户SQL与底层数据库表结构割裂开来，使得用户的SQL编写不再依赖于真实的数据库表结构。而用户与底层数据库之间的衔接、映射、转换交由ShardingSphere进行处理。

   是的，因为有logicColumn存在，用户的编写SQL都面向这个虚拟列，Encrypt-JDBC就可以把这个逻辑列和底层数据表中的密文列进行映射转换。于是迁移后的脱敏配置即为：

   ```yaml
   encryptRule:
     encryptors:
       aes_encryptor:
         type: aes
         props:
           aes.key.value: 123456abc
     tables:
       t_user:
         columns:
           pwd: # pwd与pwd_cipher的转换映射
             cipherColumn: pwd_cipher
             encryptor: aes_encryptor
    props:
       query.with.cipher.column: true
   ```

其处理流程如下：

![8](https://shardingsphere.apache.org/document/current/img/encrypt/8.png)

至此，已在线业务脱敏整改解决方案全部叙述完毕。我们提供了Java、Yaml、SpringBoot、SpringNameSpace多种方式供用户选择接入，力求满足业务不同的接入需求。该解决方案目前已在京东数科不断落地上线，提供对内基础服务支撑。

## 中间件脱敏服务优势

1. 自动化&透明化数据脱敏过程，用户无需关注脱敏中间实现细节。
2. 提供多种内置、第三方(AKS)的脱敏策略，用户仅需简单配置即可使用。
3. 提供脱敏策略API接口，用户可实现接口，从而使用自定义脱敏策略进行数据脱敏。
4. 支持切换不同的脱敏策略。
5. 针对已上线业务，可实现明文数据与密文数据同步存储，并通过配置决定使用明文列还是密文列进行查询。可实现在不改变业务查询SQL前提下，已上线系统对加密前后数据进行安全、透明化迁移。

## 适用场景说明

1. 用户项目使用Java语言进行编程。
2. 后端数据库为MySQL、Oracle、PostgreSQL、SQLServer。
3. 用户需要对数据库表中某个或多个列进行脱敏(数据加密&解密)。
4. 兼容所有常用SQL。

## 限制条件

1. 用户需要自行处理数据库中原始的存量数据、洗数。
2. 使用脱敏功能+分库分表功能，部分特殊SQL不支持，请参考[SQL使用规范]( https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/)。
3. 脱敏字段无法支持比较操作，如：大于小于、ORDER BY、BETWEEN、LIKE等。
4. 脱敏字段无法支持计算操作，如：AVG、SUM以及计算表达式               。


## Solution

ShardingSphere has provided two data masking solutions, corresponding to two ShardingSphere encryption and decryption interfaces, i.e., `ShardingEncryptor` and `ShardingQueryAssistedEncryptor`.

On the one hand, ShardingSphere has provided internal encryption and decryption implementations for users, which can be used by them only after configuration. On the other hand, to satisfy users' requirements for different scenarios, we have also opened relevant encryption and decryption interfaces, according to which, users can provide specific implementation types. Then, after simple configurations, ShardingSphere can use encryption and decryption solutions defined by users themselves to desensitize data.

### ShardingEncryptor

The solution has provided two methods, `encrypt()` and `decrypt()`, to encrypt and decrypt data to be  desensitized.

When users `INSERT`,  `DELETE` and `UPDATE`, ShardingSphere will parse, rewrite and route SQL according to the configuration. It will also use `encrypt()` to encrypt data and store them in the database. When using `SELECT`, they will decrypt sensitive data from the database with `decrypt()` reversely and return them to users at last.

Currently, ShardingSphere has provided two types of implementations for this kind of masking solution, MD5 (irreversible) and AES (reversible), which can be used after configuration.

### ShardingQueryAssistedEncryptor

Compared with the first masking scheme, this one is more secure and complex. Its concept is: even the same data, two same user passwords for example, should not be stored as the same desensitized form in the database. It can help to protect user information and avoid credential stuffing.

This scheme provides three functions to implement, `encrypt()`, `decrypt()` and `queryAssistedEncrypt()`. In `encrypt()` phase, users can set some variable, timestamp for example, and encrypt a combination of original data + variable. This method can make sure the encrypted masking data of the same original data are different, due to the existence of variables. In `decrypt()` phase, users can use variable data to decrypt according to the encryption algorithms set formerly.

Though this method can indeed increase data security, another problem can appear with it: as the same data is stored in the database in different content, users may not be able to find out all the same original data with equivalent query (`SELECT FROM table WHERE encryptedColumnn = ?`) according to this encryption column.Because of it, we have brought out assistant query column, which is generated by `queryAssistedEncrypt()`. Different from `decrypt()`, this method uses another way to encrypt the original data; but for the same original data, it can generate consistent encryption data. Users can store data processed by `queryAssistedEncrypt()` to assist the query of original data. So there may be one more assistant query column in the table.

`queryAssistedEncrypt()` and `encrypt()` can generate and store different encryption data; `decrypt()` is reversible and `queryAssistedEncrypt()` is irreversible. So when querying the original data, we will parse, rewrite and route SQL automatically. We will also use assistant query column to do `WHERE` queries and use `decrypt()` to decrypt `encrypt()` data and return them to users. All these can not be felt by users.

For now, ShardingSphere has abstracted the concept to be an interface for users to develop rather than providing accurate implementation for this kind of masking solution. ShardingSphere will use the accurate implementation of this solution provided by users to desensitize data.

## Continuance

This article describes how to use Encrypt-JDBC, one of the ShardingSphere products, SpringBoot, SpringNameSpace are also could be the access form , etc. This form of access  mainly focus to Java homogeneous, and is deployed together with business code In a production environment. For heterogeneous languages, ShardingSphere also provides Encrypt-Proxy client. Encrypt-Proxy is a server-side product that implements the binary protocol of MySQL and PostgreSQL. Users can independently deploy the Encrypt-Proxy service, User can access this `virtual database server` with decryption through third-party database management tools(e.g. Navicat), JAVA connection pool or the command line, just like access ordinary MySQL and PostgreSQL databases.

The decryption function belongs to distributed governance of Apache ShardingSphere. In fact, the Apache ShardingSphere ecosystem also has other more powerful capabilities, such as data sharding, read-write separation, distributed transactions, and monitoring governance. You can even choose any combination of these functions, such as decryption + data sharding, or data sharding + read-write separation, or monitoring governance + data sharding. In addition to the combination of these functions, ShardingSphere also provides various access forms, such as Sharding-JDBC and Sharding-Proxy for different situations.

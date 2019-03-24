+++
title = "发布指南"
weight = 4
chapter = true
+++

## GPG设置

### 安装GPG

在[GnuPG官网](https://www.gnupg.org/download/index.html)下载安装包。
GnuPG的1.x版本和2.x版本的命令有细微差别，下列说明以`GnuPG-2.1.23`版本为例。

安装完成后，执行以下命令查看版本号。

```shell
gpg --version
```

### 创建key

安装完成后，执行以下命令创建key。

GnuPG-2.x可使用：

```shell
gpg --full-gen-key
```

GnuPG-1.x可使用：

```shell
gpg --gen-key
```

根据提示完成key：

```shell
gpg (GnuPG) 2.0.12; Copyright (C) 2009 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
  (1) RSA and RSA (default)
  (2) DSA and Elgamal
  (3) DSA (sign only)
  (4) RSA (sign only)
Your selection? 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048) 4096
Requested keysize is 4096 bits
Please specify how long the key should be valid.
        0 = key does not expire
     <n>  = key expires in n days
     <n>w = key expires in n weeks
     <n>m = key expires in n months
     <n>y = key expires in n years
Key is valid for? (0) 
Key does not expire at all
Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: ${输入用户名}
Email address: ${输入邮件地址}
Comment: ${输入注释}
You selected this USER-ID:
   "${输入的用户名} (${输入的注释}) <${输入的邮件地址}>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
You need a Passphrase to protect your secret key. # 输入密码
```

### 查看生成的key

```shell
gpg --list-keys
```

执行结果：

```shell
pub   4096R/700E6065 2019-03-20
uid                  ${用户名} (${注释}) <{邮件地址}>
sub   4096R/0B7EF5B2 2019-03-20
```

其中700E6065为公钥ID。

### 将公钥同步到服务器

命令如下：

```shell
gpg --keyserver hkp://pool.sks-keyservers.net --send-key 700E6065
```

`pool.sks-keyservers.net`为随意挑选的[公钥服务器](https://sks-keyservers.net/status/)，每个服务器之间是自动同步的，选任意一个即可。

## 发布Apache Maven中央仓库

### 设置settings.xml文件

将以下模板添加到 `~/.m2/settings.xml`中，所有密码需要加密后再填入。
加密设置可参考[这里](http://maven.apache.org/guides/mini/guide-encryption.html)。

```xml
<settings>
  <servers>
    <server>
      <id>apache.snapshots.https</id>
      <username> <!-- APACHE LDAP 用户名 --> </username>
      <password> <!-- APACHE LDAP 加密后的密码 --> </password>
    </server>
    <server>
      <id>apache.releases.https</id>
      <username> <!-- APACHE LDAP 用户名 --> </username>
      <password> <!-- APACHE LDAP 加密后的密码 --> </password>
    </server>
  </servers>
</settings>
```

### 继承Apache父POM

Apache的父POM会为<distributionManagement>设置默认值，如果项目的POM中有这个标签，则需要删除。

```xml
<parent>
  <groupId>org.apache</groupId>
  <artifactId>apache</artifactId>
  <version>21</version>
</parent>
```

### 创建发布分支

假设从github下载的ShardingSphere源代码在`~/incubator-shardingsphere/`目录；假设即将发布的版本为`4.0.0-RC`。
创建`4.0.0-RC1-release`分支，接下来的操作都在该分支进行。

```shell
cd ~/incubator-shardingsphere/
git pull
git branch 4.0.0-RC1-release
git push origin 4.0.0-RC1-release
git checkout 4.0.0-RC1-release
```

### 发布预校验

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DdryRun=true -Dusername=${Github用户名}
```

-Prelease: 选择release的profile，这个profile会打包所有源码、jar文件以及sharding-proxy的可执行二进制包。

-DautoVersionSubmodules=true：作用是发布过程中版本号只需要输入一次，不必为每个子模块都输入一次。

-DdryRun=true：演练，即不产生版本号提交，不生成新的tag。

### 准备发布

首先清理发布预校验本地信息。

```shell
mvn release:clean
```

然后准备执行发布。

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DpushChanges=false -Dusername=${Github用户名}
```

和上一步演练的命令基本相同，去掉了-DdryRun=true参数。

-DpushChanges=false：不要将修改后的版本号和tag自动提交至Github。

将本地文件检查无误后，提交至github。

```shell
git push
git push origin --tags
```

### 部署发布

```shell
mvn release:perform -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -Dusername=${Github用户名}
```

执行完该命令后，待发布版本会自动上传到Apache的临时筹备仓库(staging repository)。
访问https://repository.apache.org/#stagingRepositories, 使用Apache的LDAP账户登录后，就会看到上传的版本。
点击`Close`来告诉Nexus这个构建已经完成，只有这样该版本才是可用的。
如果电子签名等出现问题，`Close`会失败，可以通过`Activity`查看失败信息。

## 发布Apache SVN仓库

### 检出shardingsphere发布目录

如无本地工作目录，则先创建本地工作目录。

```shell
mkdir -p ~/ss_svn/dev/
cd ~/ss_svn/dev/
```

创建完毕后，从Apache SVN检出shardingsphere发布目录。

```shell
svn --username=${APACHE LDAP 用户名} co https://dist.apache.org/repos/dist/dev/incubator/shardingsphere
cd ~/ss_svn/dev/shardingsphere
```

### 添加gpg公钥

仅第一次部署的账号需要添加，只要`KEYS`中包含已经部署过的账户的公钥即可。

```shell
gpg -a --export ${GPG用户名} >> KEYS
```

### 将待发布的内容添加至SVN目录

创建版本号目录。

```shell
mkdir -p ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cd ~/ss_svn/dev/shardingsphere/4.0.0-RC1
```

将源码包、二进制包和sharding-proxy可执行二进制包添加至SVN工作目录。

```shell
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-basic-distribution/target/*.zip ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-basic-distribution/target/*.zip.asc ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target/*.tar.gz ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target/*.tar.gz.asc ~/ss_svn/dev/shardingsphere/4.0.0-RC1
```

### 生成文件签名

```shell
shasum -a 512 apache-shardingsphere-incubating-4.0.0-RC1-src.zip > apache-shardingsphere-incubating-4.0.0-RC1-src.zip.sha512
shasum -b -a 512 apache-shardingsphere-incubating-4.0.0-RC1-bin.zip > apache-shardingsphere-incubating-4.0.0-RC1-bin.zip.sha512
shasum -b -a 512 apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz > apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz.sha512
```

### 提交Apache SVN

```shell
svn add *
svn --username=${APACHE LDAP 用户名} commit -m "release 4.0.0-RC1"
```
## 检查发布结果

### 检查sha512哈希

```shell
shasum -c apache-shardingsphere-incubating-4.0.0-RC1-src.zip.sha512
shasum -c apache-shardingsphere-incubating-4.0.0-RC1-bin.zip.sha512
shasum -c apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz.sha512
```

### 检查gpg签名

首先导入发布人公钥。从svn仓库导入KEYS到本地环境。（发布版本的人不需要再导入，帮助做验证的人需要导入，用户名填发版人的即可）

```shell
curl https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/KEYS >> KEYS
gpg --import KEYS
gpg --edit-key "${发布人的gpg用户名}"
  > trust
  > save
```

然后进行gpg签名检查。

```shell
gpg --verify apache-shardingsphere-incubating-4.0.0-RC1-src.zip.asc apache-shardingsphere-incubating-4.0.0-RC1-src.zip
gpg --verify apache-shardingsphere-incubating-4.0.0-RC1-bin.zip.asc apache-shardingsphere-incubating-4.0.0-RC1-bin.zip
gpg --verify apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz.asc apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz
```

### 检查发布文件内容

#### 检查源码包的文件内容

解压缩`apache-shardingsphere-incubating-4.0.0-RC1-src.zip`，进行如下检查:

- 文件夹包含单词`incubating`
- 存在`DISCLAIMER`文件
- 存在`LICENSE`和`NOTICE`文件
- 只存在文本文件，不存在二进制文件
- 所有文件的开头都有ASF许可证
- 能够正确编译，单元测试可以通过 (mvn install)
- 版本内容与Github上tag的内容相符 (diff -r a verify_dir tag_dir)
- 检查是否有多余文件或文件夹，例如空文件夹等

#### 检查二进制包的文件内容

解压缩`apache-shardingsphere-incubating-4.0.0-RC1-bin.zip`和`apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz`，进行如下检查:

- 文件夹包含单词`incubating`
- 存在`DISCLAIMER`文件
- 存在`LICENSE`和`NOTICE`文件
- 所有文本文件开头都有ASF许可证
- `sharding-proxy`二进制包配置正确后能够运行 (./start.sh)
- 检查第三方依赖许可证：
  - 第三方依赖的许可证兼容
  - 所有第三方依赖的许可证都在`LICENSE`文件中声名
  - 依赖许可证的完整版全部在`license`目录
  - 如果依赖的是Apache许可证并且存在`NOTICE`文件，那么这些`NOTICE`文件也需要加入到版本的`NOTICE`文件中

全部的检查列表参见[这里](https://wiki.apache.org/incubator/IncubatorReleaseChecklist)。

## 发起投票

### 投票阶段

1. ShardingSphere社区投票，发起投票邮件到`dev@shardingsphere.apache.org`。PPMC需要先按照文档检查版本的正确性，然后再进行投票。
经过至少72小时并统计到3个`+1 binding`票后（只有PPMC的票才是binding），即可进入下一阶段的投票。

2. Apache社区投票，发起投票邮件到`general@incubator.apache.org`。经过至少72小时并统计到3个`+1 binding`票后（只有IPMC的票才是binding），即可进行正式发布。

3. 宣布投票结果,发起投票结果邮件到`general@incubator.apache.org`。

### 投票模板

1. ShardingSphere社区投票模板

标题：
```
[VOTE]: Release Apache ShardingSphere (Incubating) 4.0.0 [RC1]
```

正文：

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere (Incubating) version 4.0.0-RC1

Release notes:
https://github.com/apache/incubator-shardingsphere/releases/edit/untagged-90bdf1e5cbba8422332f

The release candidates:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/4.0.0-RC1/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/staging/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/incubator-shardingsphere/tree/4.0.0-RC1

Release Commit ID:
https://github.com/apache/incubator-shardingsphere/commit/90a17fd3ac5af99d0fe1bd8018ba1393b1864672

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/KEYS

The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve 
[ ] +0 no opinion 
[ ] -1 disapprove with the reason
```

2. Apache社区投票邮件模板：

标题：
```
[VOTE]: Release Apache ShardingSphere (Incubating) 4.0.0 [RC1]
```

正文：

```
Hello all,

This is a call for vote to release Apache ShardingSphere (Incubating) version 4.0.0-RC1.

The Apache ShardingSphere community has voted on and approved a proposal to release
Apache ShardingSphere (Incubating) version 4.0.0-RC1.

We now kindly request the Incubator PMC members review and vote on this
incubator release.

ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 2 independent products, Sharding-JDBC & Sharding-Proxy. 
They both provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language. 
Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on Java, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Based on any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of database that conforms to JDBC standard: MySQL，Oracle，SQLServer and PostgreSQL for now.

Sharding-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the MySQL/PostgreSQL version provided now can use any kind of client access (such as MySQL Command Client, MySQL Workbench, Navicat etc.) that is compatible of MySQL/PostgreSQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL and PostgreSQL.

* Applicable to any kind of compatible of client end that is compatible of MySQL and PostgreSQL protocol.


ShardingSphere community vote and result thread:
https://lists.apache.org/thread.html/xxxxxxxxxxxxxxxxxxxxxxx

Release notes:
https://github.com/apache/incubator-shardingsphere/releases/edit/untagged-90bdf1e5cbba8422332f

The release candidates:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/4.0.0-RC1/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/staging/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/incubator-shardingsphere/tree/4.0.0-RC1

Release Commit ID:
https://github.com/apache/incubator-shardingsphere/commit/90a17fd3ac5af99d0fe1bd8018ba1393b1864672

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/KEYS

Look at here for how to verify this release candidate:
https://github.com/apache/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

The vote will be open for at least 72 hours or until necessary number of
votes are reached.

Please vote accordingly:
[ ] +1 approve
[ ] +0 no opinion
[ ] -1 disapprove with the reason
```

3. 宣布投票结果模板：

标题：
```
[RESULT] [VOTE]: Release Apache ShardingSphere (Incubating) 4.0.0 [RC1]
```

正文：

```
We’ve received 3 +1 binding votes and one +1 non-binding vote:

+1 binding, xxx
+1 binding, xxx
+1 binding, xxx

+1 non-binding, xxx

Thank you everyone for taking the time to review the release and help us. 
I will process to publish the release and send ANNOUNCE.
```

## 完成发布

1. 将源码和二进制包从svn的dev目录移动到release目录

```shell
svn mv https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/4.0.0-RC1/ https://dist.apache.org/repos/dist/release/incubator/shardingsphere/
```

2. 在Apache Staging仓库找到ShardingSphere并点击`Release`

3. 合并Github的release分支到dev

4. 发送邮件到`general@incubator.apache.org`和`dev@shardingsphere.apache.org`通知完成版本发布。

通知邮件模板：

标题：

```
[ANN] Apache ShardingSphere 4.0.0 [RC1] available
```

正文：

```
Hi all,

Apache ShardingSphere (incubating) Team is glad to announce the first release of Apache ShardingSphere Incubating 4.0.0-RC1.

ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 2 independent products, Sharding-JDBC & Sharding-Proxy. 
They both provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language. 
Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on Java, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Based on any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of database that conforms to JDBC standard: MySQL，Oracle，SQLServer and PostgreSQL for now.

Sharding-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the MySQL/PostgreSQL version provided now can use any kind of client access (such as MySQL Command Client, MySQL Workbench, Navicat etc.) that is compatible of MySQL/PostgreSQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL and PostgreSQL.

* Applicable to any kind of compatible of client end that is compatible of MySQL and PostgreSQL protocol.


Vote Thread: 

Download Links: 

Release Notes: 

Website: https://shardingsphere.apache.org/

SkyWalking Resources:
- Issue: https://github.com/apache/incubator-shardingsphere/issues
- Mailing list: dev@shardingsphere.apache.org
- Documents: https://github.com/apache/incubator-shardingsphere/blob/dev/README.md
```

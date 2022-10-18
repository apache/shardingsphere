+++
title = "ShardingSphere 发布指南"
weight = 1
chapter = true
+++

## 准备工作

### 1. 确认 Release Note

Release Note 需提供中文/英文两种版本，确认中英文描述是否明确，并按以下标签进行分类：

1. 新功能
1. API 变更
1. 功能增强
1. 漏洞修复

### 2. 确认 Issue 列表

打开 [Github Issues](https://github.com/apache/shardingsphere/issues)，过滤 Milestone 为 `${RELEASE.VERSION}` 且状态为打开的 Issue:

1. 关闭已完成的 Issue；
1. 未完成的 Issue 与负责人进行沟通，如果不影响本次发版，修改 Milestone 为下一个版本；
1. 确认发布版本的 Milestone 下没有打开状态的 Issue。

### 3. 确认 Pull Request 列表

打开 [Github Pull requests](https://github.com/apache/shardingsphere/pulls)，过滤 Milestone 为 `${RELEASE.VERSION}` 且状态为打开的 Pull Request:

1. 对打开的 Pull Request 进行 Review 并且 Merge；
1. 无法 Merge 且不影响本次发版的 Pull Request，修改 Milestone 为下一个版本；
1. 确认发布版本的 Milestone 下没有打开状态的 Pull Request。

### 4. 发送讨论邮件

1. 创建 [GitHub Discussion](https://github.com/apache/shardingsphere/discussions) 并在讨论内容中列出 Release Note；
1. 发送邮件至 [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org)，在邮件正文中链接 GitHub Discussion；
1. 关注邮件列表，确认社区开发者对 Release Note 没有任何疑问。

### 5. 关闭 Milestone

打开 [Github Milestone](https://github.com/apache/shardingsphere/milestones) 

1. 确认 `${RELEASE.VERSION}` 的 Milestone 完成状态为 100%；
1. 点击 `Close` 关闭 Milestone。

## GPG 设置

### 1. 安装 GPG

在 [GnuPG 官网](https://www.gnupg.org/download/index.html)下载安装包。
GnuPG 的 1.x 版本和 2.x 版本的命令有细微差别，下列说明以 `GnuPG-2.1.23` 版本为例。

安装完成后，执行以下命令查看版本号。

```shell
gpg --version
```

### 2. 创建 key

安装完成后，执行以下命令创建 key。

`GnuPG-2.x` 可使用：

```shell
gpg --full-gen-key
```

`GnuPG-1.x` 可使用：

```shell
gpg --gen-key
```

根据提示完成 key：

> 注意：请使用 Apache mail 生成 GPG 的 Key。

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

### 3. 查看生成的 key

```shell
gpg --list-keys
```

执行结果：

```shell
pub   4096R/700E6065 2019-03-20
uid                  ${用户名} (${注释}) <{邮件地址}>
sub   4096R/0B7EF5B2 2019-03-20
```

其中 700E6065 为公钥 ID。

### 4. 将公钥同步到服务器

命令如下：

```shell
gpg --keyserver hkp://keyserver.ubuntu.com --send-key 700E6065
```

`keyserver.ubuntu.com` 为随意挑选的公钥服务器，每个服务器之间是自动同步的，选任意一个即可。

## 准备发布分支

### 1. 创建发布分支

假设从 Github 下载的 ShardingSphere 源代码在 `~/shardingsphere/` 目录；假设即将发布的版本为 `${RELEASE.VERSION}`。
创建 `${RELEASE.VERSION}-release` 分支，接下来的操作都在该分支进行。

```shell
## ${name} 为源码所在分支，如：master，dev-4.x
git clone --branch ${name} https://github.com/apache/shardingsphere.git ~/shardingsphere
cd ~/shardingsphere/
git pull
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```

### 2. 更新版本说明和示例版本

在发布分支上更新如下文件，并提交 PR 到发布分支：

```
https://github.com/apache/shardingsphere/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md
```

更新 `examples` 模块的 pom，将版本由 `${DEVELOPMENT.VERSION}` 替换为 `${RELEASE.VERSION}`，并提交 PR 到发布分支。

### 3. 更新下载页面

更新如下页面：
* <https://shardingsphere.apache.org/document/current/en/downloads/>
* <https://shardingsphere.apache.org/document/current/cn/downloads/>

GPG 签名文件和哈希校验文件的下载连接应该使用这个前缀：`https://downloads.apache.org/shardingsphere/`。

### 4. 更新 ShardingSphere-JDBC Spring 文档中 xsd 文件链接

更新目录 `docs/document/content/user-manual/shardingsphere-jdbc/spring-namespace` 下文档内的所有 xsd 链接。

将：
```
http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-${PREVIOUS.RELEASE.VERSION}.xsd
```

更新为：
```
http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-${RELEASE.VERSION}.xsd
```

参考命令：
```shell
cd docs/document/content/user-manual/shardingsphere-jdbc/spring-namespace
grep -l -r "${PREVIOUS.RELEASE.VERSION}" . | xargs sed -i -e "s/${PREVIOUS.RELEASE.VERSION}/${RELEASE.VERSION}/g"
```

在文档中指定 xsd 版本，而不是直接使用 `sharding.xsd`，是为了让历史版本的文档能够对应到正确版本的 xsd 文件。

### 5. 修改 README 文件

更新 `README.md` 和 `README_ZH.md` 里的 `${RELEASE.VERSION}` 和 `${NEXT.RELEASE.VERSION}`。

## 发布 Apache Maven 中央仓库

### 1. 设置 settings.xml 文件

将以下模板添加到 `~/.m2/settings.xml` 中，所有密码需要加密后再填入。
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

### 2. 发布预校验

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests -Dspotless.apply.skip=true" -DautoVersionSubmodules=true -DdryRun=true -Dusername=${Github用户名}
```

-Prelease：选择 release 的 profile，这个 profile 会打包所有源码、jar 文件以及 ShardingSphere-Proxy 的可执行二进制包。

-DautoVersionSubmodules=true：作用是发布过程中版本号只需要输入一次，不必为每个子模块都输入一次。

-DdryRun=true：演练，即不产生版本号提交，不生成新的 tag。

### 3. 准备发布

首先清理发布预校验本地信息。

```shell
mvn release:clean
```

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests -Dspotless.apply.skip=true" -DautoVersionSubmodules=true -DpushChanges=false -Dusername=${Github 用户名}
```

和上一步演练的命令基本相同，去掉了 -DdryRun=true 参数。

-DpushChanges=false：不要将修改后的版本号和 tag 自动提交至 Github。

**按照 [检查发布结果](#检查发布结果) 步骤，将本地文件检查无误后**，提交至 Github。

```shell
git push origin ${RELEASE.VERSION}-release
git push origin --tags
```

### 4. 部署发布

```shell
mvn release:perform -Prelease -Darguments="-DskipTests -Dspotless.apply.skip=true" -DautoVersionSubmodules=true -Dusername=${Github 用户名}
```

执行完该命令后，待发布版本会自动上传到 Apache 的临时筹备仓库 (staging repository)。
访问 https://repository.apache.org/#stagingRepositories，使用 Apache 的 LDAP 账户登录后，就会看到上传的版本，`Repository` 列的内容即为 ${STAGING.REPOSITORY}。
点击 `Close` 来告诉 Nexus 这个构建已经完成，只有这样该版本才是可用的。
如果电子签名等出现问题，`Close` 会失败，可以通过 `Activity` 查看失败信息。

## 发布 Apache SVN 仓库

### 1. 检出 ShardingSphere 发布目录

如无本地工作目录，则先创建本地工作目录。

```shell
mkdir -p ~/ss_svn/dev/
cd ~/ss_svn/dev/
```

创建完毕后，从 Apache SVN 检出 ShardingSphere 发布目录。

```shell
svn --username=${APACHE LDAP 用户名} co https://dist.apache.org/repos/dist/dev/shardingsphere
cd ~/ss_svn/dev/shardingsphere
```

### 2. 添加 gpg 公钥

仅第一次部署的账号需要添加，只要 `KEYS` 中包含已经部署过的账户的公钥即可。

```shell
gpg -a --export ${GPG用户名} >> KEYS
```

### 3. 将待发布的内容添加至 SVN 目录

创建版本号目录。

```shell
mkdir -p ~/ss_svn/dev/shardingsphere/${RELEASE.VERSION}
cd ~/ss_svn/dev/shardingsphere/${RELEASE.VERSION}
```

将源码包、二进制包和 ShardingSphere-Proxy 可执行二进制包添加至 SVN 工作目录。

```shell
cp -f ~/shardingsphere/shardingsphere-distribution/shardingsphere-src-distribution/target/*.zip* ~/ss_svn/dev/shardingsphere/${RELEASE.VERSION}
cp -f ~/shardingsphere/shardingsphere-distribution/shardingsphere-jdbc-distribution/target/*.tar.gz* ~/ss_svn/dev/shardingsphere/${RELEASE.VERSION}
cp -f ~/shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target/*.tar.gz* ~/ss_svn/dev/shardingsphere/${RELEASE.VERSION}
cp -f ~/shardingsphere/shardingsphere-agent/shardingsphere-agent-distribution/target/*.tar.gz* ~/ss_svn/dev/shardingsphere/${RELEASE.VERSION}
```

### 4. 提交 Apache SVN

```shell
svn add * --parents
svn --username=${APACHE LDAP 用户名} commit -m "release ${RELEASE.VERSION}"
```

## 检查发布结果

### 1. 检查 sha512 哈希

```shell
shasum -c *.sha512
```

### 2. 检查 gpg 签名

首先导入发布人公钥。从 svn 仓库导入 KEYS 到本地环境。（发布版本的人不需要再导入，帮助做验证的人需要导入，用户名填发版人的即可）

```shell
curl https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS >> KEYS
gpg --import KEYS
gpg --edit-key "${发布人的 gpg 用户名}"
  > trust

Please decide how far you trust this user to correctly verify other users' keys
(by looking at passports, checking fingerprints from different sources, etc.)

  1 = I don't know or won't say
  2 = I do NOT trust
  3 = I trust marginally
  4 = I trust fully
  5 = I trust ultimately
  m = back to the main menu

Your decision? 5

  > save
```

然后进行 gpg 签名检查。

Bash 可以使用以下命令检查签名：
```bash
for each in $(ls *.asc); do gpg --verify $each ${each%.asc}; done
```

或逐个文件检查：
```shell
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-src.zip.asc apache-shardingsphere-${RELEASE.VERSION}-src.zip
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-jdbc-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-jdbc-bin.tar.gz
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-agent-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-agent-bin.tar.gz
```

### 3. 检查发布文件内容

**3.1 对比源码包与 Github 上 tag 的内容差异**

```
curl -Lo tag-${RELEASE.VERSION}.zip https://github.com/apache/shardingsphere/archive/${RELEASE.VERSION}.zip
unzip tag-${RELEASE.VERSION}.zip
unzip apache-shardingsphere-${RELEASE.VERSION}-src.zip
diff -r apache-shardingsphere-${RELEASE.VERSION}-src-release shardingsphere-${RELEASE.VERSION}
```

**3.2 检查源码包的文件内容**

- 检查源码包是否包含由于包含不必要文件，致使 tarball 过于庞大；
- 存在 `LICENSE` 和 `NOTICE` 文件；
- `NOTICE` 文件中的年份正确；
- 只存在文本文件，不存在二进制文件；
- 所有文件的开头都有 ASF 许可证；
- 能够正确编译，单元测试可以通过（./mvnw -T 1C install）；
- 检查是否有多余文件或文件夹，例如空文件夹等。

**3.3 检查二进制包的文件内容**

解压缩
- `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-jdbc-bin.tar.gz`
- `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz`
- `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-agent-bin.tar.gz`

进行如下检查:

- 存在 `LICENSE` 和 `NOTICE` 文件；
- `NOTICE` 文件中的年份正确；
- 所有文本文件开头都有 ASF 许可证；
- 检查第三方依赖许可证：
  - 第三方依赖的许可证兼容；
  - 所有第三方依赖的许可证都在 `LICENSE` 文件中声明；
  - 依赖许可证的完整版全部在 `license` 目录；
  - 如果依赖的是 Apache 许可证并且存在 `NOTICE` 文件，那么这些 `NOTICE` 文件也需要加入到版本的 `NOTICE` 文件中。

## 发起投票

**投票阶段**

1. ShardingSphere 社区投票，发起投票邮件到 `dev@shardingsphere.apache.org`。PMC 需要先按照文档检查版本的正确性，然后再进行投票。
经过至少 **72 小时** 并统计到 **3 个 `+1 PMC member`** 票后，即可进入下一阶段的投票。

2. 宣布投票结果，发起投票结果邮件到 [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org)。

**投票模板**

1. ShardingSphere 社区投票模板

标题：

```
[VOTE] Release Apache ShardingSphere ${RELEASE.VERSION}
```

正文：

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md

The release candidates:
https://dist.apache.org/repos/dist/dev/shardingsphere/${RELEASE.VERSION}/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/${STAGING.REPOSITORY}/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/shardingsphere/tree/${RELEASE.VERSION}/

Release Commit ID:
https://github.com/apache/shardingsphere/commit/xxxxxxxxxxxxxxxxxxxxxxx

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS

Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/involved/release/shardingsphere/

GPG user ID:
${YOUR.GPG.USER.ID}

The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve 

[ ] +0 no opinion
 
[ ] -1 disapprove with the reason

PMC vote is +1 binding, all others is +1 non-binding.

Checklist for reference:

[ ] Download links are valid.

[ ] Checksums and PGP signatures are valid.

[ ] Source code distributions have correct names matching the current release.

[ ] LICENSE and NOTICE files are correct for each ShardingSphere repo.

[ ] All files have license headers if necessary.

[ ] No compiled archives bundled in source archive.
```

> 注意：`Release Commit ID` 使用发布分支上与 `prepare release ${RELEASE.VERSION}` 日志对应的 commit id。 

2. 宣布投票结果模板：

标题：

```
[RESULT][VOTE] Release Apache ShardingSphere ${RELEASE.VERSION}
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

### 1. 将源码、二进制包以及 KEYS 从 svn 的 dev 目录移动到 release 目录

```shell
svn mv https://dist.apache.org/repos/dist/dev/shardingsphere/${RELEASE.VERSION} https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer packages for ${RELEASE.VERSION}"
svn delete https://dist.apache.org/repos/dist/release/shardingsphere/KEYS -m "delete KEYS"
svn cp https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer KEYS for ${RELEASE.VERSION}"
```

### 2. 在 Apache Staging 仓库找到 ShardingSphere 并点击 `Release`

### 3. 发布 Docker

3.1 准备工作

本地安装 Docker，并启动服务。

（如果使用 Docker Desktop，可以跳过该步骤）配置 QEMU：
```shell
docker run --privileged --rm tonistiigi/binfmt --install all
```

参考文档：[Docker Buildx: Build multi-platform images](https://docs.docker.com/buildx/working-with-buildx/#build-multi-platform-images)

3.2 登录 Docker Registry

```shell
docker login
```

3.3 构建并推送 ShardingSphere-Proxy Docker image

```shell
git checkout ${RELEASE.VERSION}
./mvnw -pl distribution/proxy -B -Prelease,docker.buildx.push clean package
```

3.4 确认发布成功

查看 [Docker Hub](https://hub.docker.com/r/apache/shardingsphere-proxy/) 是否有发布的镜像，确保镜像同时支持 `linux/amd64` 和 `linux/arm64`。

### 4. GitHub版本发布

在 [GitHub Releases](https://github.com/apache/shardingsphere/releases) 页面的 `${RELEASE.VERSION}` 版本上点击 `Edit`。

编辑版本号及版本说明，并点击 `Publish release`。

### 5. 从发布区移除上一版本内容

[**发布区**](https://dist.apache.org/repos/dist/release/shardingsphere/) 中仅保留一个最新的版本。
确认 [Archive repository](https://archive.apache.org/dist/shardingsphere/) 中存在上一版本后，从 [**发布区**](https://dist.apache.org/repos/dist/release/shardingsphere/) 删除上一版本：

```shell
svn del -m "Archiving release ${PREVIOUS.RELEASE.VERSION}" https://dist.apache.org/repos/dist/release/shardingsphere/${PREVIOUS.RELEASE.VERSION}
```

历史版本会自动归档到 [Archive repository](https://archive.apache.org/dist/shardingsphere/)。

孵化阶段历史版本会自动归档到 [Incubator Archive repository](https://archive.apache.org/dist/incubator/shardingsphere/)。

参考：[Release Download Pages for Projects](https://infra.apache.org/release-download-pages.html)。

### 6. 上传 Spring namespace xsd 文件至官方网站

提交 pull request 将源码中的 xsd 文件以及发布版本的 xsd 文件上传至 https://github.com/apache/shardingsphere-doc/tree/asf-site/schema/shardingsphere

应上传的文件列表如下：

- datasource.xsd
- datasource-${RELEASE.VERSION}.xsd  
- mode/standalone/repository.xsd
- mode/standalone/repository-${RELEASE.VERSION}.xsd  
- mode/cluster/repository.xsd
- mode/cluster/repository-${RELEASE.VERSION}.xsd  
- sharding.xsd
- sharding-${RELEASE.VERSION}.xsd  
- encrypt.xsd
- encrypt-${RELEASE.VERSION}.xsd  
- readwrite-splitting.xsd
- readwrite-splitting-${RELEASE.VERSION}.xsd  
- shadow.xsd
- shadow-${RELEASE.VERSION}.xsd  
- database-discovery.xsd
- database-discovery-${RELEASE.VERSION}.xsd
- sql-parser.xsd
- sql-parser-${RELEASE.VERSION}.xsd
- sql-translator.xsd
- sql-translator-${RELEASE.VERSION}.xsd

### 7. 官网首页增加发布版本文档入口

参考以下代码：
- [英文首页](https://github.com/apache/shardingsphere-doc/blob/10fb1b5f610fe2cac00c66abe2df7a8cc30c2a18/index.html#L88-L126)
- [中文首页](https://github.com/apache/shardingsphere-doc/blob/10fb1b5f610fe2cac00c66abe2df7a8cc30c2a18/index_zh.html#L88-L125)

### 8. 更新示例版本

更新 examples 模块的 pom，将版本由 ${RELEASE.VERSION} 替换为 ${NEXT.DEVELOPMENT.VERSION}，并提交 PR 到发布分支。

### 9. 合并 GitHub 的 release 分支到 `master`，合并完成后删除 release 分支

确认下载页面中的新发布版本的链接可用后，在 GitHub 页面创建 Pull Request 将分支 `${RELEASE.VERSION}-release` 合并到 `master`。
如果代码存在冲突，可以先把 master 分支合并到 `${RELEASE.VERSION}-release`。

### 10. 邮件通知版本发布完成

发送邮件到 `dev@shardingsphere.apache.org` 和 `announce@apache.org` 通知完成版本发布。

通知邮件模板（使用纯文本模式）：

标题：

```
[ANNOUNCE] Apache ShardingSphere ${RELEASE.VERSION} available
```

正文：

```
Hi all,

Apache ShardingSphere Team is glad to announce the new release of Apache ShardingSphere ${RELEASE.VERSION}.

Apache ShardingSphere is an open source ecosystem that allows you to transform any database into a distributed database system.
The project includes a JDBC and a Proxy, and its core adopts a micro-kernel and pluggable architecture.
Thanks to its plugin-oriented architecture, features can be flexibly expanded at will.

The project is committed to providing a multi-source heterogeneous, enhanced database platform and further building an ecosystem around the upper layer of the platform.
Database Plus, the design philosophy of Apache ShardingSphere, aims at building the standard and ecosystem on the upper layer of the heterogeneous database.
It focuses on how to make full and reasonable use of the computing and storage capabilities of existing databases rather than creating a brand new database.
It attaches greater importance to the collaboration between multiple databases instead of the database itself.

Download Links: https://shardingsphere.apache.org/document/current/en/downloads/

Release Notes: https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md

Website: https://shardingsphere.apache.org/

ShardingSphere Resources:
- Issue: https://github.com/apache/shardingsphere/issues/
- Mailing list: dev@shardingsphere.apache.org
- Documents: https://shardingsphere.apache.org/document/current/



- Apache ShardingSphere Team

```

## 附录：如何中止发布流程

当发布过程中发现问题，需要中止发布流程并待问题修复后重新发布，可以参考以下流程进行。

### 在投票邮件回复 -1 并说明中止原因

在 `[VOTE]` 邮件回复 -1，并说明中止投票的原因。

### 从 dev 区域移除 release candidates

```shell
svn del https://dist.apache.org/repos/dist/dev/shardingsphere/${RELEASE.VERSION} -m "Drop ${RELEASE.VERSION} release candidates"
```

### Drop Maven Staging Repository

在 <https://repository.apache.org/#stagingRepositories> 勾选本次发布的 Staging Repository，点击 **Drop** 按钮。

### Reset 发布分支并删除 tag

Reset `${RELEASE.VERSION}-release` 分支到 `maven-release-plugin` 自动提交之前的 commit：
```shell
git checkout ${RELEASE.VERSION}-release
git reset --hard ${COMMIT_ID_BEFORE_RELEASE}
git push origin --force
```

删除 tag：
```shell
git tag -d ${RELEASE.VERSION}
git push origin -d ${RELEASE.VERSION}
```

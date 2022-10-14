+++
title = "ShardingSphere on Cloud 发布指南"
weight = 2
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

打开 [Github Issues](https://github.com/apache/shardingsphere-on-cloud/issues)，过滤 Milestone 为 `${RELEASE.VERSION}` 且状态为打开的 Issue:

1. 关闭已完成的 Issue；
1. 未完成的 Issue 与负责人进行沟通，如果不影响本次发版，修改 Milestone 为下一个版本；
1. 确认发布版本的 Milestone 下没有打开状态的 Issue。

### 3. 确认 Pull Request 列表

打开 [Github Pull requests](https://github.com/apache/shardingsphere-on-cloud/pulls)，过滤 Milestone 为 `${RELEASE.VERSION}` 且状态为打开的 Pull Request:

1. 对打开的 Pull Request 进行 Review 并且 Merge；
1. 无法 Merge 且不影响本次发版的 Pull Request，修改 Milestone 为下一个版本；
1. 确认发布版本的 Milestone 下没有打开状态的 Pull Request。

### 4. 发送讨论邮件

1. 发送邮件至 [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org)，在邮件正文中链接 GitHub Discussion；
1. 关注邮件列表，确认社区开发者对 Release Note 没有任何疑问。

### 5. 关闭 Milestone

打开 [Github Milestone](https://github.com/apache/shardingsphere-on-cloud/milestones)

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

### 4. 导出 v1 版本密钥

``` shell
gpg --export >~/.gnupg/pubring.gpg
gpg --export-secret-keys >~/.gnupg/secring.gpg
```

### 5. 将公钥同步到服务器

命令如下：

```shell
gpg --keyserver hkp://keyserver.ubuntu.com --send-key 700E6065
```

`keyserver.ubuntu.com` 为随意挑选的公钥服务器，每个服务器之间是自动同步的，选任意一个即可。

## 准备发布分支

### 1. 创建发布分支

假设从 Github 下载的 ShardingSphere-On-Cloud 源代码在 `~/shardingsphere-on-cloud/` 目录；假设即将发布的版本为 `${RELEASE.VERSION}`。
创建 `${RELEASE.VERSION}-release` 分支，接下来的操作都在该分支进行。

```shell
## ${name} 为源码所在分支，如：master，dev-4.x
git clone --branch ${name} https://github.com/apache/shardingsphere-on-cloud.git ~/shardingsphere-on-cloud
cd ~/shardingsphere-on-cloud/
git pull
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```

### 2. 更新 charts 版本

在发布分支上更新如下 `Chart.yaml` 文件中的版本：

```
~/shardingsphere-on-cloud/charts/shardingsphere-operator/Chart.yaml
~/shardingsphere-on-cloud/charts/shardingsphere-operator-cluster/Chart.yaml
~/shardingsphere-on-cloud/charts/shardingsphere-proxy/Chart.yaml
```

将 `version` 修改为 `${RELEASE.VERSION}`，`appVersion` 修改为对应的应用版本，并提交 PR 到发布分支。

### 3. 打包 charts

```shell
cd ~/shardingsphere-on-cloud/charts
helm package --sign --key ${用户名} --keyring /.gnupg/secring.gpg  `变动的 charts`
```

### 4. 更新下载页面

更新如下页面：
* <https://shardingsphere.apache.org/document/current/en/downloads/>
* <https://shardingsphere.apache.org/document/current/cn/downloads/>

GPG 签名文件和哈希校验文件的下载连接应该使用这个前缀：`https://downloads.apache.org/shardingsphere/`。

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
cd ~/ss_svn/dev/shardingsphere/charts
```

### 2. 添加 gpg 公钥

仅第一次部署的账号需要添加，只要 `KEYS` 中包含已经部署过的账户的公钥即可。

```shell
gpg -a --export ${GPG用户名} >> KEYS
```

### 3. 将待发布的内容添加至 SVN 目录

创建版本号目录。

```shell
mkdir -p ~/ss_svn/dev/shardingsphere/charts/${RELEASE.VERSION}
cd ~/ss_svn/dev/shardingsphere/charts/${RELEASE.VERSION}
```

将 charts 包添加至 SVN 工作目录。

```shell
cp -f ~/shardingsphere-on-cloud/charts/*.tgz
~/ss_svn/dev/shardingsphere/charts/${RELEASE.VERSION}
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

```shell
helm verify `变动的charts打包文件`
```

### 3. 检查发布文件内容

**3.1 对比源码包与 Github 上 tag 的内容差异**

```shell
curl -Lo tag-apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz https://github.com/apache/shardingsphere-on-cloud/archive/apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz
diff -r tag-apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz  apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz

curl -Lo tag-shardingsphere-cluster-${RELEASE.VERSION}.tgz https://github.com/apache/shardingsphere-on-cloud/archive/shardingsphere-cluster-${RELEASE.VERSION}.tgz
diff -r tag-shardingsphere-cluster-${RELEASE.VERSION}.tgz  shardingsphere-cluster-${RELEASE.VERSION}.tgz

curl -Lo tag-shardingsphere-operator-${RELEASE.VERSION}.tgz https://github.com/apache/shardingsphere-on-cloud/archive/shardingsphere-operator-${RELEASE.VERSION}.tgz
diff -r tag-shardingsphere-operator-${RELEASE.VERSION}.tgz  shardingsphere-operator-${RELEASE.VERSION}.tgz
```

**3.2 检查源码包的文件内容**

- 检查源码包是否包含由于包含不必要文件，致使 tarball 过于庞大；
- 存在 `LICENSE` 和 `NOTICE` 文件；
- `NOTICE` 文件中的年份正确；
- 只存在文本文件，不存在二进制文件；
- 所有文件的开头都有 ASF 许可证；
- 能够正确安装 helm install
- 检查是否有多余文件或文件夹，例如空文件夹等。

**3.3 检查 Charts 包的文件内容**

解压缩
- `apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz`
- `shardingsphere-cluster-${RELEASE.VERSION}.tgz`
- `shardingsphere-operator-${RELEASE.VERSION}.tgz`

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

This is a call for vote to release Apache ShardingSphere On Cloud version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere-on-cloud/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md


Git tag for the release:
https://github.com/apache/shardingsphere-on-cloud/tree/${RELEASE.VERSION}/

Release Commit ID:
https://github.com/apache/shardingsphere-on-cloud/commit/xxxxxxxxxxxxxxxxxxxxxxx

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

3. 邮件通知版本发布完成

````
发送邮件到 `dev@shardingsphere.apache.org` 和 `announce@apache.org` 通知完成版本发布。

通知邮件模板：

标题：

```
[ANNOUNCE] Apache ShardingSphere On-Cloud-${RELEASE.VERSION} available
```

正文：

```
Hi all,

Apache ShardingSphere Team is glad to announce the new release of Apache ShardingSphere On-Cloud-${RELEASE.VERSION}.

The shardingsphere-on-cloud project, including ShardingSphere Operator, Helm Charts, and other cloud solutions, aims at enhancing the deployment and management capabilities of Apache ShardingSphere Proxy on the cloud. 
ShardingSphere Operator is a Kubernetes software extension written with the Operator extension pattern of Kubernetes. ShardingSphere Operator can be used to quickly deploy an Apache ShardingSphere Proxy cluster in the Kubernetes environment and manage the entire cluster life cycle.


Release Notes: https://github.com/apache/shardingsphere-on-cloud/blob/master/RELEASE-NOTES.md




- Apache ShardingSphere Team

```
````
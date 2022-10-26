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


## 准备发布分支

### 1. 创建发布分支

假设从 Github 下载的 `ShardingSphere on Cloud` 源代码在 `~/shardingsphere-on-cloud/` 目录；假设即将发布的版本为 `${RELEASE.VERSION}`。
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

在发布分支上更新 `Chart.yaml` 文件中的版本：

```
~/shardingsphere-on-cloud/charts/shardingsphere-operator/Chart.yaml
~/shardingsphere-on-cloud/charts/shardingsphere-operator-cluster/Chart.yaml
~/shardingsphere-on-cloud/charts/shardingsphere-proxy/Chart.yaml
```

将 `version` 修改为 `${RELEASE.VERSION}`，`appVersion` 修改为对应的应用版本，并提交 PR 到发布分支。

### 3. 创建发布 tag

在发布分支上创建发布 tag，并提交 PR 到发布分支。

```shell
git tag ${RELEASE.VERSION}
git push origin --tags
```

### 4. 打包 charts

打包 charts 之前需要通过 `helm dependency build` 命令下载依赖的包，然后再对 charts 进行打包，具体操作步骤如下：

```shell
cd ~/shardingsphere-on-cloud/charts/shardingsphere-operator
helm dependency build

cd ~/shardingsphere-on-cloud/charts/shardingsphere-operator-cluster
helm dependency build

cd ~/shardingsphere-on-cloud/charts/shardingsphere-proxy/charts/governance
helm dependency build

cd ~/shardingsphere-on-cloud/charts/shardingsphere-proxy
helm dependency build

cd ~/shardingsphere-on-cloud/charts
helm package shardingsphere-operator
helm package shardingsphere-operator-cluster
helm package shardingsphere-proxy
```

### 5. 更新下载页面

更新如下页面：
* <https://shardingsphere.apache.org/document/current/en/downloads/>
* <https://shardingsphere.apache.org/document/current/cn/downloads/>


## 检查发布结果

### 1. 检查发布文件内容

解压缩
- `apache-shardingsphere-operator-charts-${RELEASE.VERSION}.tgz`
- `apache-shardingsphere-operator-cluster-charts-${RELEASE.VERSION}.tgz`
- `apache-shardingsphere-proxy-charts-${RELEASE.VERSION}.tgz`

进行如下检查:

- 存在 `LICENSE` 和 `NOTICE` 文件；
- `NOTICE` 文件中的年份正确；
- 所有文本文件开头都有 ASF 许可证；
- 检查第三方依赖许可证：
  - 第三方依赖的许可证兼容；
  - 所有第三方依赖的许可证都在 `LICENSE` 文件中声明；
  - 依赖许可证的完整版全部在 `license` 目录；
  - 如果依赖的是 Apache 许可证并且存在 `NOTICE` 文件，那么这些 `NOTICE` 文件也需要加入到版本的 `NOTICE` 文件中。
### 2. 检查仓库制品

添加仓库
```shell
helm repo remove apache
helm repo add apache  https://apache.github.io/shardingsphere-on-cloud
helm search repo apache
```

可以查询到三个制品即为发布成功,`helm repo add` 和 `helm search repo` 会根据 index.yaml 中的校验值进行校验 

```shell
NAME                                              	CHART VERSION	           APP VERSION	DESCRIPTION
apache/apache-shardingsphere-operator-charts     	${RELEASE.VERSION}       	xxx     	A Helm chart for ShardingSphere-Operator
apache/apache-shardingsphere-operator-cluster-...	${RELEASE.VERSION}        	xxx      	A Helm chart for ShardingSphere-Operator-Cluster
apache/apache-shardingsphere-proxy-charts        	${RELEASE.VERSION}        	xxx         A Helm chart for ShardingSphere-Proxy-Cluster
```

## 发起投票

**投票阶段**

1. ShardingSphere 社区投票，发起投票邮件到 `dev@shardingsphere.apache.org`。PMC 需要先按照文档检查版本的正确性，然后再进行投票。
经过至少 **72 小时** 并统计到 **3 个 `+1 PMC member`** 票后，即可进入下一阶段的投票。

2. 宣布投票结果，发起投票结果邮件到 [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org)。

**投票模板**

1. ShardingSphere 社区投票模板

标题：

```
[VOTE] Release Apache ShardingSphere on Cloud ${RELEASE.VERSION}
```

正文：

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere on Cloud version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere-on-cloud/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md


Git tag for the release:
https://github.com/apache/shardingsphere-on-cloud/tree/${RELEASE.VERSION}/

Release Commit ID:
https://github.com/apache/shardingsphere-on-cloud/commit/xxxxxxxxxxxxxxxxxxxxxxx


Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/involved/release/shardingsphere-on-cloud/


The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve 

[ ] +0 no opinion
 
[ ] -1 disapprove with the reason

PMC vote is +1 binding, all others is +1 non-binding.

Checklist for reference:

[ ] Source code distributions have correct names matching the current release.

[ ] LICENSE and NOTICE files are correct for each ShardingSphere on Cloud repo.

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
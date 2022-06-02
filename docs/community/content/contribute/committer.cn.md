+++
title = "提交者指南"
weight = 4
chapter = true
+++

## 提交者提名

ShardingSphere 社区遵循 [Apache Community’s process](http://community.apache.org/newcommitter.html) 来接收新的提交者。
当您积极地参与 ShardingSphere 社区之后，项目管理委员会和项目官方提交者会根据您的表现发起吸纳您成为官方提交者和项目管理委员会成员的流程。

## 提交者责任

 - 开发新功能；
 - 代码重构；
 - 及时和可靠的评审 Pull Request；
 - 思考和接纳新特性请求；
 - 解答问题；
 - 维护文档和代码示例；
 - 改进流程和工具；
 - 定期检查 [CI Scheduled Workflow]( https://github.com/apache/shardingsphere/actions?query=event%3Aschedule ) 是否正常运行；
 - 引导新的参与者融入社区。

## 日常工作

1. Committer 需要每天查看社区待处理的 Pull Request 和 Issue 列表，负责问题的处理：

 - 包括标记 issue，回复 issue，关闭 issue 等；
 - 将 issue 分配至熟悉该模块的贡献者；

2. Assignee 在被分配 issue 后，需要进行如下判断：

 - 判断是否是长期 issue，如是，则标记为 pending；
 - 判断 issue 类型，如：bug，enhancement，discussion 等；
 - 标记 Milestone。

3. Committer 提交的 PR，需要根据 PR 类型和当前发布的周期标注 Label 和 Milestone。

4. Committer review PR 时，可以进行 squash and merge to master 的操作，如果有问题可以加上 change request 或者 @ 相关人员协助处理。

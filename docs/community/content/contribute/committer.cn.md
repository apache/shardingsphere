+++
title = "提交者指南"
weight = 3
chapter = true
+++

## 提交者提名

ShardingSphere社区遵循[Apache Community’s process](http://community.apache.org/newcommitter.html) 来接收新的提交者。
当您积极地参与ShardingSphere社区之后，项目管理委员会和项目官方提交者会根据您的表现发起吸纳您成为官方提交者和项目管理委员会成员的流程。

## 提交者责任

 - 开发新功能；
 - 代码重构；
 - 及时和可靠的评审Pull Request；
 - 思考和接纳新特性请求；
 - 解答问题；
 - 维护文档和代码示例；
 - 改进流程和工具；
 - 引导新的参与者融入社区。

## 日常工作

1. 每周负责轮值的Committer需要每天查看社区待处理的Pull Request和Issue列表，负责问题的处理。

 - 包括（标记issue，回复issue，关闭issue）。
 - 将issue assign给熟悉该模块的Contributor/Committer，即Assignee。
 
> 轮值Committer名单在Committer交流群里实时更新，轮值岗位设定目的是发挥大家的主人翁意识，增进归属感，ShardingSphere社区属于每一位PMC/Committer。

2. Assignee在被分配issue后，需要进行如下判断：

 - 判断是否是长期issue，如是，则标记为pending。
 - 判断issue类型，如：bug，enhancement，discussion等。
 - 判断Milestone，并标记。

3. Committer提交的Pull Request，需要根据PR类型和时间标注label 和 milestone。

4. Committer review Pull Request时，可以进行squash and merge to master的操作, 如果有问题可以加上 change request 或者@相关人员协助处理。

> 注意: 无论是否是社区issue，都必须有assignee，直到issue完成。

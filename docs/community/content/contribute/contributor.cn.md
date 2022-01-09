+++
title = "贡献者指南"
weight = 3
chapter = true
+++

您可以报告bug，提交一个新的功能增强建议或者直接对以上内容提交改进补丁。

## 提交issue

 - 在提交issue之前，请经过充分的搜索，确定该issue不是通过简单的检索即可以解决的问题。
 - 查看[issue列表](https://github.com/apache/shardingsphere/issues)，确定该issue不是一个重复的问题。
 - [新建](https://github.com/apache/shardingsphere/issues/new/choose)一个issue并选择您的issue类型。
 - 使用一个清晰并有描述性的标题来定义issue。
 - 根据模板填写必要信息。
 - 在提交issue之后，对该issue分配合适的标签。如：bug，enhancement，discussion等。
 - 请对自己提交的issue保持关注，在讨论中进一步提供必要信息。

## 开发流程

**1. 准备仓库**

到 [ShardingSphere GitHub Repo]( https://github.com/apache/shardingsphere ) fork 仓库到你的 GitHub 账号。

克隆到本地。
```shell
git clone https://github.com/(your_github_name)/shardingsphere.git
```

添加 ShardingSphere 远程仓库。
```shell
git remote add apache https://github.com/apache/shardingsphere.git
git remote -v
```

**2. 选择issue**

 - 请在选择您要修改的issue。如果是您新发现的问题或想提供issue中没有的功能增强，请先新建一个issue并设置正确的标签。
 - 在选中相关的issue之后，请回复以表明您当前正在这个issue上工作。并在回复的时候为自己设置一个deadline，添加至回复内容中。
 - 在[开发者列表](/cn/contribute/contributor/)中找到一个导师，导师会在设计与功能实现上给予即时的反馈。

**3. 创建分支**

 - 切换到fork的master分支，拉取最新代码，创建本次的分支。

```shell
git checkout master
git fetch apache
git rebase apache/master
git push origin master # 可选操作
git checkout -b issueNo
```

 **注意** ：PR 会按照 squash 的方式进行 merge。如果不创建新分支，本地和远程的提交记录将不能保持同步。

**4. 编码**

 - 请您在开发过程中遵循ShardingSphere的[开发规范](/cn/contribute/code-conduct/)。并在准备提交pull request之前完成相应的检查。
 - 将修改的代码push到fork库的分支上。

```shell
git add 修改代码
git commit -m 'commit log'
git push origin issueNo
```

**5. 提交PR**

 - 发送一个pull request到ShardingSphere的master分支。
 - 接着导师做CodeReview，然后他会与您讨论一些细节（包括设计，实现，性能等）。当导师对本次修改满意后，会将提交合并到当前开发版本的分支中。
 - 最后，恭喜您已经成为了ShardingSphere的官方贡献者！

**6. 删除分支**

 - 在导师将pull request合并到ShardingSphere的master分支中之后，您就可以将远程的分支（origin/issueNo）及与远程分支（origin/issueNo）关联的本地分支（issueNo）删除。
 
```shell
git checkout master
git branch -d issueNo
git remote prune origin # 如果你已经在 GitHub PR 页面删除了分支，否则的话可以执行下面的命令删除
git push origin --delete issueNo
```

**注意**: 为了让您的id显示在contributor列表中，别忘了以下设置：

```shell
git config --global user.name "username"
git config --global user.email "username@mail.com"
```

+++
title = "贡献者指南"
weight = 1
chapter = true
+++

您可以报告 bug，提交一个新的功能增强建议或者直接对以上内容提交改进补丁。

## 提交 issue

 - 在提交 issue 之前，请经过充分的搜索，确定该 issue 不是通过简单的检索即可以解决的问题。
 - 查看 [issue 列表](https://github.com/apache/shardingsphere/issues)，确定该 issue 不是一个重复的问题。
 - [新建](https://github.com/apache/shardingsphere/issues/new/choose)一个 issue 并选择您的 issue 类型。
 - 使用一个清晰并有描述性的标题来定义 issue。
 - 根据模板填写必要信息。
 - 在提交 issue 之后，对该 issue 分配合适的标签。如：bug，enhancement，discussion 等。
 - 请对自己提交的 issue 保持关注，在讨论中进一步提供必要信息。

## 开发流程

**1. 准备仓库**

到 [ShardingSphere GitHub Repo]( https://github.com/apache/shardingsphere ) fork 仓库到你的 GitHub 账号。

克隆到本地。

```shell
git clone https://github.com/(your_github_name)/shardingsphere.git
```

添加 ShardingSphere 远程仓库。

```shell
cd shardingsphere
git remote add apache https://github.com/apache/shardingsphere.git
git remote -v
```

编译并安装所有模块到 Maven 本地仓库缓存，同时会生成 ANTLR `.g4` 语法文件对应的解析器 Java 类，这样在 IDE 就不会有相关的编译错误了。

```shell
cd shardingsphere
./mvnw clean install -DskipITs -DskipTests -P-dev,release,all
```

当你以后从 ShardingSphere 拉取最新代码并新建分支，可能会遇到类似的解析器编译错误，可以重新运行这个命令来解决问题。

**2. 选择 issue**

 - 请在选择您要修改的 issue。如果是您新发现的问题或想提供 issue 中没有的功能增强，请先新建一个 issue 并设置正确的标签。
 - 在选中相关的 issue 之后，请回复以表明您当前正在这个 issue 上工作。并在回复的时候为自己设置一个 deadline，添加至回复内容中。
 - 在[开发者列表](/cn/team/)中找到一个导师，导师会在设计与功能实现上给予即时的反馈。

**3. 创建分支**

 - 切换到 fork 的 master 分支，拉取最新代码，创建本次的分支。

```shell
git checkout master
git fetch apache
git rebase apache/master
git push origin master # 可选操作
git checkout -b issueNo
```

 **注意** ：PR 会按照 squash 的方式进行 merge。如果不创建新分支，本地和远程的提交记录将不能保持同步。

**4. 编码**

 - 请您在开发过程中遵循 ShardingSphere 的[开发规范](/cn/involved/conduct/code/)。并在准备提交 pull request 之前完成相应的检查。
 - 将修改的代码 push 到 fork 库的分支上。

```shell
git add 修改代码
git commit -m 'commit log'
git push origin issueNo
```

**5. 提交 PR**

 - 发送一个 pull request 到 ShardingSphere 的 master 分支。
 - 接着导师做 CodeReview，然后他会与您讨论一些细节（包括设计，实现，性能等）。当导师对本次修改满意后，会将提交合并到当前开发版本的分支中。
 - 最后，恭喜您已经成为了 ShardingSphere 的官方贡献者！

**6. 更新 Release Note**

 - 编码完成后，请更新当前开发版本的 [Release Note](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md)，根据 issue 的不同类型，在 `API Change`、`New Feature`、`Enhancement` 或 `Bug Fix` 分类中进行添加，`RELEASE-NOTES` 需要遵循统一的格式：`{feature_name}: {description} - {issue/pr link}`，例如：`SQL Parser: Support PostgreSQL, openGauss function table and update from segment parse - #32994`。

**7. 删除分支**

 - 在导师将 pull request 合并到 ShardingSphere 的 master 分支中之后，您就可以将远程的分支（origin/issueNo）及与远程分支（origin/issueNo）关联的本地分支（issueNo）删除。
 
```shell
git checkout master
git branch -d issueNo
git remote prune origin # 如果你已经在 GitHub PR 页面删除了分支，否则的话可以执行下面的命令删除
git push origin --delete issueNo
```

**注意**: 为了让您的 id 显示在 contributor 列表中，别忘了以下设置：

```shell
git config --global user.name "username"
git config --global user.email "username@mail.com"
```

+++
title = "贡献者指南"
weight = 2
chapter = true
+++

您可以报告bug，提交一个新的功能增强建议或者直接对以上内容提交改进补丁。

## 提交issue

 - 在提交issue之前，请经过充分的搜索，确定该issue不是通过简单的检索即可以解决的问题。
 - 查看[issue列表](https://github.com/sharding-sphere/sharding-sphere/issues)，确定该issue不是一个重复的问题。
 - [新建](https://github.com/sharding-sphere/sharding-sphere/issues/new)一个issue。
 - 使用一个清晰并有描述性的标题来定义issue。
 - 如果您提交的是一个bug，请尽量提供如下信息：
      - 详细的描述复现bug的步骤。包括您使用的SQL，配置情况，预计产生的结果，实际产生的结果。并附加详细的TRACE日志。
      - ShardingSphere以及操作系统版本。
      - 在github提供用于可以复现问题的项目演示代码。
      - 如果程序抛出异常，请附加完整的堆栈日志。
      - 如有可能，请附上屏幕截图或动态的GIF图，这些图片能帮助演示整个问题的产生过程。
      - 如果涉及性能问题，请附加上CPU，内存或网络磁盘IO的Profile截图。
 - 如果您提交的是一个建议，请尽量提供如下信息：
      - 详细描述增强功能的行为模式。
      - 解释说明为什么该功能是对大多数用户有用的。新功能应该具有广泛的适用性。
      - 如有可能，可以列出其他数据库中间已经具备的类似功能。开源与商用软件均可。
 - 在提交issue之后，对该issue分配合适的标签。如：bug，enhancement，discussion等。
 - 请对自己提交的issue保持关注，在讨论中进一步提供必要信息。
 - 如果问题已经解决，请关闭该issue。如果您不及时关闭，我们将在三天后将其关闭。
 - 如果问题有新的进展，请将之前关闭的issue再次开启。请注意，只有您自己关闭的issue可以再次开启，逾期而被我们关闭的issue您将没有再次开启该issue的权限。

## 提交pull request

 - 请在选择您要修改的issue。如果是您新发现的问题或想提供issue中没有的功能增强，请先新建一个issue并设置正确的标签。
 - 在选中相关的issue之后，请回复以表明您当前正在这个issue上工作。并在回复的时候为自己设置一个deadline，添加至回复内容中。
 - 在[开发者列表](http://incubator.apache.org/projects/shardingsphere.html)中找到一个导师，导师会在设计与功能实现上给予即时的反馈。
 - 您需要fork一个分支到您自己的repo来开始工作。
 - 请您在开发过程中遵循ShardingSphere的[开发规范](/cn/contribute/code-conduct/)。并在准备提交pull request之前完成相应的检查。
 - 完成后，发送一个pull request到ShardingSphere的dev分支，请不要提交pull request至master分支中。
 - 接着导师做CodeReview，然后他会与您讨论一些细节（包括设计，实现，性能等）。当导师对本次修改满意后，会将提交合并到当前开发版本的分支中。
 - 最后，恭喜您已经成为了ShardingSphere的官方贡献者！
 - 注意，为了让你的id显示在contributor列表中，别忘了以下设置：
      - git config --global user.name "username"
      - git config --global user.email "username@mail.com"

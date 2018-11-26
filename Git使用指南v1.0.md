# Git 使用指南



**为什么要规范Git工作流程**

一个人开发做事可以随心所欲，啥都可以无所谓，但是如果是团队合作开发就需要正视一些问题，比如：代码规范、编码风格、命名规范、注释说明等等。你的一举一动会影响到小伙伴们的开发效率。而你小伙伴的一举一动会影响到你的开发效率。一段莫名其妙的代码让程序挂了，一段重要的API缺少注释，一个耦合了各种功能，代码超过3000行的类，都会让接手和调用这部分代码的同学崩溃。

因此，一个好的工作流程和约束可以帮助整个团队提高开发效率，避免一些无意义的采坑，让整个代码看上去类似一个人写的。



## 一、Commit message 格式约束



每次提交 commit message 都应该包括三个部分: Header, Body 和 Footer。

```
<type>(<scope>): <subject>
// 空一行
<body>
// 空一行
<footer>
```

其中，Header是必须的，Body和Footer可以省略。



### 1.1 Header

Header部分只有一行，包括三个字段：`type`（必须）、`scope`（可选）、`subject`（必须）。



**（1）Type [必须]**

`type`用于说明commit的类别，只允许使用下面7个标识。

- **feature**: 新功能
- **fix**: 修补bug
- **docs**: 文档修改
- **style**: 格式、分号缺失等，不影响代码运行的变动
- **refactor**: 生产代码重构（即不是新增功能，也不是修改bug的代码变动）
- **test**: 增加测试
- **chore**: 构建任务更新、程序包管理器配置、及辅助工具的变动等，对生产代码无变动



如果`type`为`feature`和`fix`中，则该commit将肯定出现在Change log之中。其他情况需要由各个项目组自行决定，参考目前github上的部分项目，一般不包含在Change log中。



**（2）Scope [可选]**

`scope`用于说明commit影响的范围，比如数据层、控制层、视图层等等。这个具体需要和每个项目的软件架构和业务需求进行关联，应该在每个项目的初期协定好，并写入项目的README中，并定期根据实践反馈的情况，对scope的可选项进行优化调整。



以项目为例，可以如下两种划分方式：

- 按照业务功能模块
  - 顶部组件，header
  - 导航组件，Menu



**（3）Subject [必须]**

`subject` 是 commit目的的简单描述，建议不要超过50个字。

**约束：**

- 尽量以祈使语气描述提交的任务，而不是其已完成的任务，使用第一人称现在时。例如，使用change，而不是changed或changes
- 首字母大写
- 末尾不加句号

**建议：**

对于应该使用中文还是英语编写message，我觉得不一定需要强制要求。但是有一个非常重要的前提，如果使用英语编写message，一定要保证语义和语法的正确，且描述通俗易懂，否则还不如使用中文表达更加直观。



### 1.2 Body

Body部分为消息体，是对本次commit的详细描述，可以分成多行。由于并不是所有的提交信息都复杂到需要主题，因此这个部分是可选内容，仅在提交信息需要一定的解释和语境时使用。

**约束：**

- Header与Body之间一定要加一个空行
- 每行内容应控制在72个字符内
- **Body是用于解释提交任务的内容和原因，重点说明代码变动的动机，而不是方法**



### 1.3 Footer

Footer部分是可选内容，只用于两种情况。



#### 1.3.1 不兼容变动

如果当前代码与上一个版本不兼容，则Footer部分以`BREAKING CHANGE`开头，后面是对变动的描述、以及变动理由和迁移方法。

```
BREAKING CHANGE: isolate scope bindings definition has changed.

    To migrate the code follow the example below:

    Before:

    scope: {
      myAttr: 'attribute',
    }

    After:

    scope: {
      myAttr: '@',
    }

    The removed `inject` wasn't generaly useful for directives so there should be no code using it.
```



#### 1.3.2 引用跟踪 Issue

如果当前 commit 针对某个issue，那么可以在Footer部分关闭这个issue。

> Closes #123



也可以一次关闭多个issue

> Closes #123, #245



### 1.4 Revert

还有一种特殊情况，如果当前 commit 用于撤销以前的 commit，则必须以 `revert:` 开头，后面跟着被撤销 Commit 的 Header。

```
revert: feature(address): 添加查询地址3公里范围内商圈类型分布JSF方法

This reverts commit 667ecc1654a317a13331b17617d973392f415f02.
```

Body部分的格式是固定的，必须写成 `This reverts commit <hash>.`，其中的`hash`是被撤销 commit 的 SHA 标识符。

**Note**：如果当前 commit 与 被撤销的 commit，在同一个发布（release）里面，那么他们都不会出现在 Change log 里面。如果两者在不同的发布，那么当前 commit，会出现在 Change log 的 `Reverts` 小标题下面。



### 1.5 提交信息示例 

```
feature: 总结变动的内容，保持在50个字符内

// 空行
如有需要，使用更详细的说明性文字，将其大概控制在72个字。在部分语境中，第一行被视为提交信息的主题，余下的文本被视为主体。分隔总结与主体的空行十分重要（除非你完全忽略主体）；否则`log`、`shortlog`和`rebase`等多个工具容易发生混淆。
解释该提交信息所解决的问题，说明你进行该变动的原因，而不是方法（代码本身可以解释方法）。
该变动是否存在副作用或其他直觉性后果？在这里进行解释。

// 空行
如果你使用问题追踪，将其引用放在末尾，例如：
Closes #123
```





## 参考

1. [Commit message 和 Change log 编写指南](http://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html)
2. [怎么写Git Commit Message](https://www.jianshu.com/p/0117334c75fc)






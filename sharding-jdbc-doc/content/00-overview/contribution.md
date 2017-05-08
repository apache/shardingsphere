+++
toc = true
date = "2017-04-12T16:06:17+08:00"
title = "贡献代码"
weight = 3
prev = "/00-overview/intro/"
next = "/01-start"

+++

您可以报告bug，提交一个新的功能增强建议或者直接对以上内容提交改进补丁。

## 报告bug

本章节介绍如何提交一个bug。

### 报告一个新bug之前

 - 确定在最新版本中该bug存在。我们将不会持续维护所有的发布版本，所有的修改仅根据当前版本。
 - 确认该bug是可以复现的。请尽量提供完整的重现步骤。
 - 请确定这不是一个重复的bug。
   查看[Issue Page](https://github.com/dangdangdotcom/sharding-jdbc/issues)列表，搜索您要提交的bug是否已经被报告过。

### 如何提交一个有质量的bug

请在[Issue Page](https://github.com/dangdangdotcom/sharding-jdbc/issues)页面中提交bug。

 - 使用一个清晰并有描述性的标题来定义bug。
 - 详细的描述复现bug的步骤。包括您使用的SQL，配置情况，预计产生的结果，实际产生的结果。并附加详细的TRACE日志。
 - 如果程序抛出异常，请附加完整的堆栈日志。
 - 如有可能，请附上屏幕截图或动态的GIF图，这些图片能帮助演示整个问题的产生过程。
 - 如果涉及性能问题，请附加上CPU，内存或网络磁盘IO的Profile截图。
 - 说明适用的版本，只有release版本的bug才可以提交，并且应该是当前最新版本。
 - 说明适用的操作系统，及其版本。
 - 使用bug标签(Label)来标记这个issue。

以下是bug的Markdown模板，请按照该模板填写issue。

```
[问题简单描述]

**问题复现步骤:**

1. [第一步]
2. [第二步]
3. [其他步骤...]

**期望的表现:**

[在这里描述期望的表现]

**观察到的表现:**

[在这里描述观察到的表现]

**屏幕截图和动态GIF图**

![复现步骤的屏幕截图和动态GIF图](图片的url)

**Sharding-JDBC版本:** [输入Sharding-JDBC的版本]
**操作系统及版本:** [输入操作系统及版本]

```

## 提交功能增强建议

本章节介绍如何提交一个功能增强建议。

### 提交一个功能增强建议之前
 
 - 请先检查[详细功能列表](/01-start/features/)。
 - 请确定这不是一个重复的功能增强建议。
   查看[Issue Page](https://github.com/dangdangdotcom/sharding-jdbc/issues)列表，搜索您要提交的功能增强建议是否已经被提交过。

### 如何提交一个好的功能增强建议

请在[Issue Page](https://github.com/dangdangdotcom/sharding-jdbc/issues)页面中提交功能增强建议。

 - 使用一个清晰并有描述性的标题来定义增强建议。
 - 详细描述增强功能的行为模式。
 - 解释说明为什么该功能是对大多数用户是有用的。新功能应该具有广泛的适用性。
 - 如有可能，可以列出其他数据库中间已经具备的类似功能。商用与开源软件均可。
 - 使用enhancement标签(Label)来标记这个issue。

以下是功能增强建议的Markdown模板，请按照该模板填写issue。

```
[简单的建议描述]

**建议的新功能行为**

[描述新功能应表现的行为模式]

**为什么这个新功能是对大多数用户有用的**

[解释这个功能为什么对大多数用户是有用的]

[列出其他的数据库中间件是否包含该功能，且如何实现的]

```

## 贡献补丁(patch)

本章节向贡献者介绍开发规范、环境、示例和文档。

### 开发理念

 - 用心写代码，提炼真正的非功能性需求。
 - 代码整洁干净到极致, 请参见《重构》和《代码整洁之道》。
 - 极简代码, 高度复用，无重复代码和配置。
 - 代码应在同一抽象层级。
 - 修改功能时多考虑影响面, 不可留下没修改完全的部分。
 - 只有一个需求时，不需扩展性。两个类似需求时, 再提炼扩展性。

### 开发行为规范

 - 提交之前先确定模块的测试套件，并使用测试覆盖率工具检查覆盖率不能低于master分支的覆盖率。
 - 使用Checkstyle检查代码, 违反验证规则的需要有特殊理由。模板位置在sharding-jdbc/src/resources/dd_checks.xml。
 - 执行mvn clean install可以测试和编译通过。
 - 及时删除无用代码。
 
### 编码规范

 - 写代码之前看一下系统已有的代码, 保持风格和使用方式一致。
 - 变量命名要有意义, 如果方法只有唯一的返回值, 使用result命名返回值. 循环中使用each命名循环变量, map中使用entry代替each。
 - 嵌套循环尽量提成方法。
 - 优先使用卫语句。
 - 配置文件使用驼峰命名, 文件名首字母小写。
 - 类和方法的访问权限控制为最小, 例如: 可以设为包私有的就不用public。
 - 方法所用到的私有方法应紧跟着该方法, 如果有多个私有方法, 书写私有方法应与私有方法在原方法的出现顺序相同。
 - 优先使用guava而非apache commons, 如：优先使用Strings而非StringUtils。
 - 优先使用lombok代替构造器, get, set, log方法。
 - 使用linux换行符。
 - 缩进（包含空行）和上一行保持一致。
 - 不应有无意义的空行。
 - 方法入参和返回值不允许为null，如有特殊情况需注释说明。
 - 需要注释解释的代码尽量提成小方法，用方法名称解释，注释应只包含javadoc和todo，fixme等。
 - 禁止使用static import。
 - 不需要公开的类放入internal包，包中类尽量包私有。
 - 日志一律使用英文。
 - 使用annotation获取spring的业务bean。
 - 如果模块中有公用的切入点，应在模块一级路径创建pointcut包。
 - 属性配置项需要添加到各个模块的常量枚举中。

### 单元测试规范

 - 测试代码和生产代码需遵守相同代码规范。
 - 如无特殊理由, 测试需全覆盖。
 - 准备环境的代码和测试代码分离。
 - 单数据断言, 应使用assertTrue, assertFalse, assertNull, assertNotNull。
 - 多数据断言, 应使用assertThat。
 - 精确断言, 尽量不使用not, containsString断言。
 - 调用业务方法的变量, 应命名为actualXXX, 期望值应命名为expectedXXX。
 - 只有junit assertXXX, hamcrest, mocktio相关可以使用static import。

### 编译代码

Sharding-JDBC的代码编译需要[Maven](http://maven.apache.org/)，请保证IDE中正确配置了它。
代码用到的所有依赖完全都可以从公网下载，请根据自身的网络情况选择合理的镜像。

代码使用了[Lombok](https://projectlombok.org/download.html)来生成类属性的访问方法，构造器等。
故请以上从链接内容来获取适合您的IDE的解决方法。

### 文档生成

文档使用博客生成引擎[HUGO](https://gohugo.io/)，请根据文档安装环境。
文档全部在sharding-jdbc/sharding-jdbc-doc/public目录中。

### 贡献方法

请按照规范贡献代码，示例和文档。

 - 所有的问题与新功能请使用[Issue Page](https://github.com/dangdangdotcom/sharding-jdbc/issues)进行管理。
 - 任何人想要开发任何功能，请先回复该功能所关联的Issue，表明您当前正在这个Issue上工作。
   并在回复的时候为自己设置一个deadline，并添加的回复内容中。
 - 在核心贡献者找到一个导师(shepherd)，导师会在设计与功能实现上给予即时的反馈。
   如果您没有熟悉的架构师，请向__sharding_jdbc@groups.163.com__发送邮件。
 - 您应该新建一个分支来开始您的工作，分支的名字为功能名称/issueId。
   例如，您想完成一个SQL解析(parser)功能中 __Issue 111__，那么您的branch名字应为 __parser/111__。
   功能名称与导师讨论后确定。
 - 完成后，发送一个pull request到dangdangdotcom/sharding-jdbc，
   接着导师做CodeReview，然后他会与您讨论一些细节（包括设计，实现，性能等）。当团队中所有人员对本次修改满意后，导师会将提交合并到master分支。
 - 最后，恭喜您已经成为了Sharding-JDBC的官方贡献者！
 
+++
title = "开发规范"
weight = 2
chapter = true
+++

以下行为准则以完全遵循 [Apache 软件基金会行为准则](https://www.apache.org/foundation/policies/conduct.html)为前提。

## 开发理念

 - **用心** 保持责任心和敬畏心，以工匠精神持续雕琢。
 - **可读** 代码无歧义，通过阅读而非调试手段浮现代码意图。
 - **整洁** 认同《重构》和《代码整洁之道》的理念，追求整洁优雅代码。
 - **一致** 代码风格、命名以及使用方式保持完全一致。
 - **精简** 极简代码，以最少的代码表达最正确的意思。高度复用，无重复代码和配置。及时删除无用代码。
 - **抽象** 层次划分清晰，概念提炼合理。保持方法、类、包以及模块处于同一抽象层级。
 - **极致** 拒绝随意，保证任何一行代码、任何一个字母、任何一个空格都有其存在价值。

## 代码提交行为规范

 - 确保遵守编码规范。
 - 确保构建流程中的各个步骤都成功完成，包括：Apache 协议文件头检查、Checkstyle 检查、编译、单元测试等。构建流程启动命令：`./mvnw clean install -B -T1C -Dmaven.javadoc.skip -Dmaven.jacoco.skip -e`。
 - 确保覆盖率不低于 master 分支。
 - 应尽量将设计精细化拆分；做到小幅度修改，多次数提交，但应保证提交的完整性。
 - 通过 Spotless 统一代码风格，执行 `mvn spotless:apply` 格式化代码。
 - 如果您使用 IDEA，可导入推荐的 `src/resources/code-style-idea.xml`。
 
## 编码规范

 - 使用 linux 换行符。
 - 不应有无意义的空行。请提炼私有方法，代替方法体过长或代码段逻辑闭环而采用的空行间隔。
 - 类、方法和变量的命名要做到顾名思义，类、方法名避免使用缩写，部分变量名可以使用缩写。
   - 变量名 `arguments` 缩写为 `args`；
   - 变量名 `parameters` 缩写为 `params`；
   - 变量名 `environment` 缩写为 `env`；
   - 变量名 `properties` 缩写为 `props`；
   - 变量名 `configuration` 缩写为 `config`。
 - 三位以内字符的专有名词缩写使用大写，超过三位字符的缩写采用驼峰形式。
   - 三位以内字符的类和方法名称缩写的示例：SQL92Lexer、XMLTransfer、MySQLAdminExecutorCreator；
   - 三位以上字符的类和方法名称缩写的示例：JdbcUrlAppender、YamlAgentConfigurationSwapper；
   - 变量应使用小驼峰形式：mysqlAuthenticationMethod、sqlStatement、mysqlConfig。
 - 除了直接返回方法入参，返回变量使用 `result` 命名；循环中使用 `each` 命名循环变量；map 中使用 `entry` 代替 `each`。
 - 捕获的异常名称命名为 `ex` ；捕获异常且不做任何事情，异常名称命名为 `ignored`。
 - 配置文件使用 `Spinal Case` 命名（一种使用 `-` 分割单词的特殊 `Snake Case`）。
 - 需要注释解释的代码尽量提成小方法，用方法名称解释。
 - `equals` 和 `==` 条件表达式中，常量在左，变量在右；大于小于等条件表达式中，变量在左，常量在右。
 - 除了构造器入参与全局变量名称相同的赋值语句外，避免使用 `this` 修饰符。
 - 除了用于继承的抽象类之外，尽量将类设计为 `final`。
 - 嵌套循环尽量提成方法。
 - 成员变量定义顺序以及参数传递顺序在各个类和方法中保持一致。
 - 优先使用卫语句。
 - 类和方法的访问权限控制为最小。
 - 方法所用到的私有方法应紧跟该方法，如果有多个私有方法，书写私有方法应与私有方法在原方法的出现顺序相同。
 - 方法入参和返回值不允许为 `null`。
 - 优先使用 lombok 代替构造器，getter, setter 方法和 log 变量。
 - 优先考虑使用 `LinkedList`，只有在需要通过下标获取集合中元素值时再使用 `ArrayList`。
 - `ArrayList`，`HashMap` 等可能产生扩容的集合类型必须指定集合初始大小，避免扩容。
 - 日志与注释一律使用英文。
 - 注释只能包含 javadoc，todo 和 fixme。
 - 公开的类和方法必须有 javadoc，对用户的 API 和 SPI 的 javadoc 需要写的清晰全面，其他类和方法以及覆盖自父类的方法无需 javadoc。
 - 优先使用三目运算符代替 if else 的返回和赋值语句。
 - 禁止嵌套使用三目运算符。
 - 条件表达式中，优先使用正向语义，以便于理解代码逻辑。例如：`if (null == param) {} else {}`。
 - 使用具体的 `@SuppressWarnings("xxx")` 代替 `@SuppressWarnings("all")`。
 - 热点方法内应避免使用 Java Stream，除非该场景下使用 Stream 的性能优于普通循环。
 - 工具类名称命名为 `xxUtils`。

## 单元测试规范

 - 测试代码和生产代码需遵守相同代码规范。
 - 单元测试需遵循 AIR（Automatic, Independent, Repeatable）设计理念。
   - 自动化（Automatic）：单元测试应全自动执行，而非交互式。禁止人工检查输出结果，不允许使用 `System.out`，`log` 等，必须使用断言进行验证。
   - 独立性（Independent）：禁止单元测试用例间的互相调用，禁止依赖执行的先后次序。每个单元测试均可独立运行。
   - 可重复执行（Repeatable）：单元测试不能受到外界环境的影响，可以重复执行。
 - 单元测试需遵循 BCDE（Border, Correct, Design, Error）设计原则。
   - 边界值测试（Border）：通过循环边界、特殊数值、数据顺序等边界的输入，得到预期结果。
   - 正确性测试（Correct）：通过正确的输入，得到预期结果。
   - 合理性设计（Design）：与生产代码设计相结合，设计高质量的单元测试。
   - 容错性测试（Error）：通过非法数据、异常流程等错误的输入，得到预期结果。
 - 除去简单的 `getter /setter` 方法，以及声明 SPI 的静态代码，如：`getType / getOrder`，单元测试需全覆盖。
 - 每个测试用例需精确断言，尽量不使用 `not`、`containsString` 断言。
 - 准备环境的代码和测试代码分离。
 - 只有 Mockito，junit `Assertions`，hamcrest `CoreMatchers` 和 `MatcherAssert` 相关可以使用 static import。
 - 数据断言规范应遵循：
    - 布尔类型断言应使用 `assertTrue` 和 `assertFalse`；
    - 空值断言应使用 `assertNull` 和 `assertNotNull`；
    - 其他类型应使用 `assertThat`。
 - 测试用例的真实值应名为为 actual XXX，期望值应命名为 expected XXX。
 - 测试类和 `@Test` 标注的方法无需 javadoc。
 - 使用 `mock` 应遵循如下规范：
   - 单元测试需要连接某个环境时，应使用 `mock`；
   - 单元测试包含不容易构建的对象时，例如：超过两层嵌套并且和测试无关的对象，应使用 `mock`。
   - 模拟静态方法或构造器，应优先考虑使用测试框架提供的 `AutoMockExtension` 和 `StaticMockSettings` 自动释放资源；若使用 Mockito `mockStatic` 和 `mockConstruction` 方法，必须搭配 `try-with-resource` 或在清理方法中关闭，避免泄漏。
   - 校验仅有一次调用时，无需使用 `times(1)` 参数，使用 `verify` 的单参数方法即可。

## G4 规范

 - 公共规范
   - 每行长度不超过 `200` 个字符，保证每一行语义完整以便于理解。
 - 词法解析规范
   - 每个规则一行，规则间无需空行。
   - 规则名称使用大写字母。如果名称由多个单词组成，用 `下划线` 间隔。`DataType` 和 `Symbol` 的规则命名以 `下划线` 结尾。与 ANTLR 内置变量或关键字重名的规则在结尾加 `下划线` 以示区分。
   - 不对外暴露的规则使用 `fragment`，`fragment` 定义的规则需在其服务的规则之后声明。
   - 公用规则定义放在 `Keyword.g4`，每个数据库可以有自己特有的规则定义。例如：`MySQLKeyword.g4`。
 - 语法解析规范
   - 每个规则结束后空一行，空行无需缩进。
   - 规则名称前面不空格，`冒号` 后空一格再开始写规则，`分号` 在单独一行并保持和上一行相同缩进。
   - 如果一个规则的分支超过 `5` 个，则每个分支一行。
   - 规则命名采用 java 变量的驼峰形式。
   - 为每种 SQL 语句类型定义一个独立的语法文件，文件名称由 `数据库名称` + `语句类型名称` + `Statement`。例如：`MySQLDQLStatement.g4`。

## GitHub Action 规范

- Workflow 文件名以 `.yml` 结尾。
- Workflow 文件名由 `触发方式-执行操作` 的小写字母组成。例如：`nightly-check.yml`。pull_request 触发的任务省略触发方式，例如：`check.yml`。
- 触发方式包括：pull_request（不加前缀）、nightly、schedule。
- 执行操作包括：check、ci、e2e 、build、report。
- Workflow 文件内的 `name` 属性命名与文件名一致，单词以 `-` 作为分隔符，分隔符两侧要加空格，每个单词首字母大写。例如：`Nightly - Check`。
- Step 下的 `name` 属性应该描述 step 的功能，每个单词首字母大写，介词小写。例如：`Build Project with Maven`。
- Workflow 中的 `job` 属性命名，须在 Workflow 中保持唯一。
- 使用 `matrix` 的时候，必须添加作业并行度限制为 20。例如：`max-parallel: 20`。
- 必须为作业设置超时时间，最大不超过 1 小时。例如：`timeout-minutes: 10`。

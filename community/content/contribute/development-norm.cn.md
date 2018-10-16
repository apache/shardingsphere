+++
title = "开发规范"
weight = 4
chapter = true
+++

## 开发理念

 - 用心写代码，追求整洁和优雅到极致的极简代码，认同《重构》和《代码整洁之道》的理念。 

## 代码提交行为规范

 - 确保通过全部测试用例。
 - 确保覆盖率不低于dev分支。
 - 确保使用Checkstyle检查代码，违反验证规则的需要有特殊理由。模板位置在`sharding-sphere/src/resources/sharding_checks.xml`，请使用checkstyle 8.8运行规则。
 - 确保执行`mvn clean install`可以编译和测试通过。
 - 及时删除无用代码。
 
## 编码规范

 - 使用linux换行符。
 - 缩进（包含空行）和上一行保持一致。
 - 不应有无意义的空行。
 - 日志与注释一律使用英文。
 - 注释只能包含javadoc，todo和fixme。
 - 变量命名要有意义。返回值变量使用result命名；循环中使用each命名循环变量；map中使用entry代替each。
 - 配置文件使用驼峰命名，文件名首字母小写。
 - 条件表达式中，常量在左，变量在右。
 - 嵌套循环尽量提成方法。
 - 优先使用卫语句。
 - 类和方法的访问权限控制为最小。
 - 方法所用到的私有方法应紧跟着该方法，如果有多个私有方法，书写私有方法应与私有方法在原方法的出现顺序相同。
 - 方法入参和返回值不允许为null。
 - 需要注释解释的代码尽量提成小方法，用方法名称解释。
 - 优先使用lombok代替构造器，getter, setter方法和log变量。
 - 熟悉系统已有的代码，保持风格和使用方式一致。
 - 高度复用，无重复代码和配置。

## 单元测试规范

 - 测试代码和生产代码需遵守相同代码规范。
 - 如无特殊理由，测试需全覆盖。
 - 准备环境的代码和测试代码分离。
 - 只有junit Assert，hamcrest CoreMatchers，Mockito相关可以使用static import。
 - 单数据断言，应使用`assertTrue`，`assertFalse`，`assertNull`和`assertNotNull`。
 - 多数据断言，应使用`assertThat`。
 - 精确断言，尽量不使用`not`，`containsString`断言。
 - 测试用例的真实值应名为为actualXXX，期望值应命名为expectedXXX。

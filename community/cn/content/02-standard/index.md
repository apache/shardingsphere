+++
date = "2018-03-05T16:06:17+08:00"
title = "开发规范"
weight = 0
prev = "/01-contribute/"
next = "/03-company/"
chapter = true
+++

# 开发规范

## 开发理念

 - 用心写代码，提炼真正的非功能性需求。
 - 代码整洁干净到极致, 请参见《重构》和《代码整洁之道》。
 - 极简代码, 高度复用，无重复代码和配置。
 - 代码应在同一抽象层级。
 - 修改功能时多考虑影响面, 不可留下没修改完全的部分。
 - 只有一个需求时，不需扩展性。两个类似需求时, 再提炼扩展性。

## 行为规范

 - 提交前确保通过全部测试用例
 - 使用测试覆盖率工具检查覆盖率不能低于dev分支的覆盖率。
 - 使用Checkstyle检查代码，违反验证规则的需要有特殊理由。模板位置在sharding-jdbc/src/resources/dd_checks.xml。
 - 执行mvn clean install可以测试和编译通过。
 - 及时删除无用代码。
 
## 编码规范

 - 使用linux换行符。
 - 缩进（包含空行）和上一行保持一致。
 - 不应有无意义的空行。
 - 日志与注释一律使用英文。
 - 禁止使用static import。
 - 变量命名要有意义。返回值变量使用result命名。循环中使用each命名循环变量，map中使用entry代替each。
 - 配置文件使用驼峰命名，文件名首字母小写。
 - 嵌套循环尽量提成方法。
 - 优先使用卫语句。
 - 类和方法的访问权限控制为最小。
 - 方法所用到的私有方法应紧跟着该方法，如果有多个私有方法，书写私有方法应与私有方法在原方法的出现顺序相同。
 - 方法入参和返回值不允许为null，如有特殊情况需注释说明。
 - 需要注释解释的代码尽量提成小方法，用方法名称解释，注释应只包含javadoc和todo，fixme等。
 - 不需要公开的类放入internal包，包中类尽量包私有。
 - 优先使用guava而非apache commons，如：优先使用Strings而非StringUtils。
 - 优先使用lombok代替构造器，get, set, log方法。
 - 熟悉系统已有的代码，保持风格和使用方式一致。

## 单元测试规范

 - 测试代码和生产代码需遵守相同代码规范。
 - 如无特殊理由, 测试需全覆盖。
 - 准备环境的代码和测试代码分离。
 - 只有junit assertXXX, hamcrest, mocktio相关可以使用static import。
 - 单数据断言, 应使用assertTrue, assertFalse, assertNull, assertNotNull。
 - 多数据断言, 应使用assertThat。
 - 精确断言, 尽量不使用not, containsString断言。
 - 调用业务方法的变量, 应命名为actualXXX, 期望值应命名为expectedXXX。

# Detailed Design: Conversation Examples

## 1. 目标

这份文档给出面向用户会话工作流的标准对话样板。

它主要服务于三类后续工作：

- MCP tool prompt 设计
- e2e 会话验收用例
- 前端或调用方的交互预期对齐

## 2. 示例约定

- `用户` 代表原始自然语言输入
- `MCP` 代表系统回复
- 上游模型已做的结构化整理不在示例里展开，但 MCP 的回复默认建立在“已知部分结构化意图 + 当前追问”的边界上
- 示例使用逻辑视图，不展开底层物理库细节
- 敏感参数在示例中默认打码

## 3. 示例一：一起做的 Encrypt Create

### 用户

```text
帮我给 order_db 的 t_order.phone 做加密，需要支持等值查询，一起做。
```

### MCP

```text
本次计划分 8 步：
1. 确认目标对象与操作类型
2. 读取当前规则、插件与逻辑元数据
3. 补齐加密能力需求
4. 选择算法
5. 采集算法参数
6. 生成 DDL、DistSQL 与索引计划
7. 审阅并执行
8. 验证并总结

我先确认两点：
1. 是否需要可逆解密？
2. 是否还需要 LIKE 查询？
```

### 用户

```text
要可逆，不需要 LIKE。
```

### MCP

```text
已确认：
- 目标：order_db.t_order.phone
- 操作：encrypt create
- 需求：可逆解密、等值查询、不需要 LIKE

推荐算法：
- 主算法：AES
- 辅助查询算法：MD5

下一步需要采集 AES 所需参数：
- aes-key-value

请提供该参数。默认会在后续 review 中打码显示。
```

### 用户

```text
aes-key-value 是 my-secret-key
```

### MCP

```text
计划已生成，准备 review：

- 逻辑列：phone
- 主算法：AES
- 辅助查询算法：MD5
- 派生列：
  - phone_cipher
  - phone_assisted_query
- 索引建议：
  - idx_t_order_phone_assisted_query
- 参数预览：
  - aes-key-value = ******

拟执行工件：
1. ALTER TABLE t_order ADD COLUMN phone_cipher VARCHAR(4000), ADD COLUMN phone_assisted_query VARCHAR(4000)
2. CREATE INDEX idx_t_order_phone_assisted_query ON t_order (phone_assisted_query)
3. CREATE ENCRYPT RULE ...

请确认是否执行。
```

### 用户

```text
执行。
```

### MCP

```text
执行进度：
- 物理列 DDL：passed
- 索引 DDL：passed
- Encrypt DistSQL：passed

验证结果：
- DDL：passed
- Rule：passed
- Logical Metadata：passed
- SQL Executability：passed

最终逻辑入口仍为：t_order.phone
```

## 4. 示例二：一步一步的 Encrypt Alter

### 用户

```text
给 order_db 的 t_order.phone 现有加密规则补一个等值查询能力，一步一步做。
```

### MCP

```text
本次按一步一步模式推进。

全局步骤：
1. 读取当前 encrypt 规则
2. 判断需要新增哪些派生列和索引
3. 选择或确认算法
4. 采集参数
5. 生成 review 工件
6. 等待你确认执行
7. 验证并汇总

我先去读取当前规则。
```

### MCP

```text
第 1 步已完成。

发现当前规则仅包含：
- logic column: phone
- cipher column: phone_cipher

还缺：
- assisted_query column
- assisted_query algorithm

下一步需要你确认：
- 是否仍使用 AES 作为主加密算法？
- 是否接受新增派生列和索引？
```

### 用户

```text
AES 不变，接受新增。
```

### MCP

```text
第 2 步确认完成。

默认新增：
- phone_assisted_query
- idx_t_order_phone_assisted_query

下一步请确认辅助查询算法，推荐：
- MD5
```

### 用户

```text
用 MD5。
```

### MCP

```text
第 3 步完成。

当前变更计划：
1. 新增 phone_assisted_query 列
2. 新建辅助查询索引
3. ALTER ENCRYPT RULE

如需继续，请回复“生成 review”。
```

## 5. 示例三：Manual-only 模式

### 用户

```text
给 order_db.t_order.phone 做加密，支持等值查询，但不要自动执行，只把 SQL 给我。
```

### MCP

```text
已切换为 manual-only 模式。

我仍会先完成：
1. 步骤清单
2. 元数据与算法确认
3. 参数采集
4. 工件生成

但不会自动执行任何 SQL 或 DistSQL。
```

### 用户

```text
算法用 AES，辅助查询用 MD5，密钥你按我刚给的参数生成。
```

### MCP

```text
review-safe 预览：
- 主算法：AES
- 辅助查询算法：MD5
- 参数预览：
  - aes-key-value = ******

可执行工件包已准备：
1. ALTER TABLE ...
2. CREATE INDEX ...
3. CREATE ENCRYPT RULE ...

当前状态：awaiting-manual-execution
我不会自动执行。你手工执行完成后，可以继续让我做四层验证。
```

## 6. 示例四：Mask Create

### 用户

```text
给 order_db 的 t_order.phone 做手机号脱敏，显示时保留前 3 后 4。
```

### MCP

```text
本次识别为 mask create。

全局步骤：
1. 确认目标对象
2. 读取当前 mask 规则与插件
3. 确认字段语义和展示效果
4. 选择算法并采集参数
5. 生成 rule-only review
6. 执行
7. 验证

推荐算法：
- MASK_FROM_X_TO_Y

需要确认参数：
- from-x = 3
- to-y = 7
- replace-char = *
```

## 7. 示例五：Mask Drop

### 用户

```text
把 order_db 的 t_order.phone 脱敏规则删掉。
```

### MCP

```text
本次识别为 mask drop。

全局步骤：
1. 读取当前 mask 规则
2. 确认目标规则存在
3. 生成 DROP MASK RULE 工件
4. review
5. 执行
6. 验证规则已删除且逻辑 SQL 可执行

我先检查当前规则是否存在。
```

### MCP

```text
目标规则存在。

拟执行工件：
- DROP MASK RULE t_order

这次不会生成物理 DDL。
请确认是否执行。
```

## 8. 示例六：自定义 SPI 算法能力不完整

### 用户

```text
给 order_db.t_order.phone 加密，用我们自定义的 MY_AES_PLUS。
```

### MCP

```text
我在当前 Proxy 插件池里发现了 MY_AES_PLUS。

但当前只能确认：
- 算法可见

还不能自动确认：
- 是否支持解密
- 是否支持等值查询
- 是否支持 LIKE 查询

请补充你的能力需求。如果你仍坚持使用该算法，我会把它标记为“能力待确认”继续规划。
```

## 9. 示例七：Encrypt Drop

### 用户

```text
把 order_db.t_order.phone 的加密规则删掉。
```

### MCP

```text
本次识别为 encrypt drop。

全局步骤：
1. 读取当前 encrypt 规则
2. 确认目标规则存在
3. 生成 DROP ENCRYPT RULE 工件
4. review
5. 执行
6. 验证规则删除、逻辑元数据与逻辑 SQL 可执行性

先说明一点：
- 本次不会恢复历史明文数据
- 如果后续需要清理遗留物理列或索引，请你自行处理
```

### MCP

```text
目标规则存在。

拟执行工件：
- DROP ENCRYPT RULE t_order

本次只生成规则删除工件，不生成 cleanup DDL。

如确认继续，我会执行规则删除，并在之后做规则、逻辑元数据和逻辑 SQL 可执行性验证。
```

## 10. 示例八：命名冲突自动改名

### 用户

```text
给 order_db.t_order.phone 加密，支持等值查询。
```

### MCP

```text
规划时发现默认派生列名冲突：
- phone_cipher 已存在

已自动改名为：
- phone_cipher_1
- phone_assisted_query

warning:
- code: WF-NAME-002
- message: 默认派生列名发生冲突，系统已自动改名

请在 review 中确认最终名称。
```

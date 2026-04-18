# Quickstart: ShardingSphere-Proxy Encrypt and Mask MCP V1

## 1. 适用范围

本 quickstart 说明 V1 期望给用户提供的标准交互体验。
它不是实现代码，而是未来 MCP 工作流的验收样板。

## 2. 前置条件

- 已接入 `ShardingSphere-Proxy`
- MCP 运行时已能访问目标 Proxy
- 目标逻辑库、逻辑表、逻辑列存在
- 用户能明确给出 `database`
- 如果需要自动执行物理 DDL，当前连接具备相应权限

## 3. 调用边界

- 用户可以通过自然语言发起需求。
- 上游模型负责尽量把自然语言整理成结构化意图。
- MCP 负责规则相关澄清、算法推荐、DDL / DistSQL / 索引规划、执行和验证。
- 因此本流程的重点不是“让 MCP 自己做强语义理解”，而是“让 MCP 把规则相关的事情做对”。

## 4. 所有请求的统一开场

无论是加密还是脱敏、创建还是删除，工作流都必须先返回一份全局步骤清单。

### 示例：全局步骤清单

1. 确认数据库、表、列和目标能力
2. 读取当前规则、算法插件与逻辑元数据
3. 补齐缺失信息并推荐算法
4. 生成命名方案、DDL、DistSQL 与索引建议
5. 等待用户确认执行模式
6. 执行 DDL 与规则变更
7. 验证 DDL、规则、逻辑元数据与 SQL 可执行性
8. 返回最终总结

如果用户选择“一起做”，系统沿这张清单连续推进。
如果用户选择“一步一步做”，系统每完成一步都停下来等待下一次确认。
用户不需要在每一步重新发送之前已经确认过的上下文，系统应在服务端记住当前计划状态。
这份上下文只要求在当前服务运行期内可续做，不要求服务重启后恢复。

## 5. 场景 A：新增加密规则

### 用户输入

```text
帮我给 order_db 里的 t_order.phone 做加密，查询时要支持等值匹配，最好一起做。
```

### 系统必须先追问的典型问题

- 是否要求可逆解密？
- 是否还需要 `LIKE` 查询？
- 物理 DDL 希望自动执行、审阅后执行，还是只生成？
- 是否接受为辅助查询列生成索引？

### 规划输出示例

```text
目标: 为 order_db.t_order.phone 创建加密规则
逻辑列: phone
推荐算法:
- 主加密算法: AES
- 辅助查询算法: MD5

默认派生列:
- phone_cipher
- phone_assisted_query

拟执行步骤:
1. 校验 t_order.phone 是否存在且当前无冲突规则
2. 生成物理列 DDL
3. 生成索引 DDL
4. 生成 CREATE ENCRYPT RULE DistSQL
5. 按所选模式执行
6. 执行后验证四层状态
```

### 物理 DDL 示例

```sql
ALTER TABLE t_order
  ADD COLUMN phone_cipher VARCHAR(4000),
  ADD COLUMN phone_assisted_query VARCHAR(4000);
```

```sql
CREATE INDEX idx_t_order_phone_assisted_query
ON t_order (phone_assisted_query);
```

### DistSQL 示例

```sql
CREATE ENCRYPT RULE t_order (
COLUMNS(
(NAME=phone,CIPHER=phone_cipher,ASSISTED_QUERY=phone_assisted_query,
ENCRYPT_ALGORITHM(TYPE(NAME='AES', PROPERTIES('aes-key-value'='***', 'digest-algorithm-name'='SHA-1'))),
ASSISTED_QUERY_ALGORITHM(TYPE(NAME='MD5')))
));
```

### 完成后的验证摘要示例

```text
DDL 验证: passed
规则验证: passed
逻辑元数据验证: passed
逻辑 SQL 可执行性验证: passed
最终逻辑入口: t_order.phone
最终物理列:
- phone_cipher
- phone_assisted_query
```

## 6. 场景 B：新增脱敏规则

### 用户输入

```text
给 order_db 的 t_order.phone 做手机号脱敏，结果展示时保留前 3 后 4。
```

### 规划输出示例

```text
目标: 为 order_db.t_order.phone 创建脱敏规则
推荐算法: MASK_FROM_X_TO_Y
原因: 该字段语义接近手机号，且展示诉求是保留首尾部分字符
本次不需要物理列 DDL
```

### DistSQL 示例

```sql
CREATE MASK RULE t_order (
COLUMNS(
(NAME=phone,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=3, "to-y"=7, "replace-char"="*")))
));
```

### 验证摘要示例

```text
DDL 验证: skipped
规则验证: passed
逻辑元数据验证: passed
逻辑 SQL 可执行性验证: passed
```

## 7. 场景 C：修改现有规则

### 修改加密规则

适用于以下情况：

- 原来只做了密文列，现在补充辅助查询列
- 需要更换算法
- 需要改派生列命名
- 原来有辅助查询列，现在要取消并决定是否清理旧列和旧索引

### 示例 DistSQL

```sql
ALTER ENCRYPT RULE t_order (
COLUMNS(
(NAME=phone,CIPHER=phone_cipher,ASSISTED_QUERY=phone_assisted_query,
ENCRYPT_ALGORITHM(TYPE(NAME='AES', PROPERTIES('aes-key-value'='***', 'digest-algorithm-name'='SHA-1'))),
ASSISTED_QUERY_ALGORITHM(TYPE(NAME='MD5')))
));
```

### 修改脱敏规则

```sql
ALTER MASK RULE t_order (
COLUMNS(
(NAME=phone,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=3, "to-y"=7, "replace-char"="*")))
));
```

### 修改流的额外要求

- 必须先读取当前规则，不能盲改
- 必须明确告诉用户哪些字段或算法发生了变化
- 如果修改牵涉新派生列或新索引，仍需经过 DDL 审阅

## 8. 场景 D：删除现有规则

### 删除加密规则

```sql
DROP ENCRYPT RULE t_order;
```

### 删除流说明

- 系统必须先确认当前 encrypt rule 存在
- 系统必须明确提示“不会恢复历史明文数据”
- 系统不会生成 cleanup DDL
- 如果后续需要删除遗留物理列或索引，由用户自行处理

### 删除脱敏规则

```sql
DROP MASK RULE t_order;
```

### 删除流说明

- `mask drop` 默认走 rule-only 删除
- `encrypt drop` 支持 rule 删除，但不包含 cleanup 规划

## 9. 三种执行模式

### 9.1 自动执行

- 系统生成 DDL / DistSQL
- 系统按步骤自动执行
- 系统逐步报告进度
- 系统自动完成验证

### 9.2 审阅后执行

- 系统先展示全部 DDL / DistSQL / 索引工件
- 用户 review 后确认
- 系统执行
- 系统自动完成验证

### 9.3 仅生成，不执行

- 系统只返回 DDL / DistSQL / 索引工件
- 默认 review 预览中的敏感参数必须打码
- 如用户明确要求，系统再返回可执行工件包
- 用户自行执行
- 系统返回后续建议验证动作

## 10. 命名冲突示例

如果 `phone_cipher` 已存在，系统不能默认复用，而应改名并回传给用户，例如：

```text
原始建议:
- phone_cipher

冲突后最终采用:
- phone_cipher_1
```

对 `assisted_query` 和 `like_query` 列同样适用。

## 11. 为什么本流程不依赖样本数据

- 当前交付目标是“把规则做对、把 DDL 做对、把验证做对”
- 完成标准只依赖规则、逻辑元数据和 SQL 可执行性
- 不把读取样本数据做成默认路径，可以降低权限风险和歧义

因此，V1 的主流程不以样本数据为前提。

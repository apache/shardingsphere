# Detailed Design: Error Codes and Failure Semantics

## 1. 目标

这份文档把工作流中的失败、阻塞、待确认状态统一成稳定的错误语义。

它解决三个问题：

- 失败时返回什么结构；
- 哪些情况算真正错误，哪些只是待确认或警告；
- 规划、执行、验证三大阶段如何给前端、测试和用户一致的输出。

## 2. 设计原则

- 所有失败都必须有稳定、可机器识别的 `code`。
- 所有失败都必须指明发生阶段。
- 所有失败都必须给出面向用户的下一步建议。
- 敏感参数不能出现在错误明文里。
- `manual-only` 未执行前是等待态，不是成功也不是失败。
- 能继续推进的情况优先使用 `warning` 或 `pending`，不要滥用 `error`。

## 3. 统一问题模型

所有 tool 在输出中都应允许携带统一的 `issues` 列表。

单个 issue 的最小字段：

- `code`
- `severity`: `warning` / `error`
- `stage`: `intaking` / `discovering` / `clarifying` / `selecting-algorithm` / `collecting-properties` / `planning-artifacts` / `review` / `executing` / `validating`
- `message`
- `userAction`
- `retryable`
- `details`

可选字段：

- `relatedArtifacts`
- `relatedField`
- `maskedContext`

## 4. 状态与问题的关系

### 4.1 `pending`

表示还缺信息或缺人工确认，不能继续执行，但不属于失败。

典型场景：

- 缺少数据库名
- 缺少算法能力需求
- 缺少必填属性
- `manual-only` 工件已生成但用户尚未执行

### 4.2 `warning`

表示当前还能继续，但存在风险或不确定性。

典型场景：

- 自定义 SPI 算法能力待确认
- 自动改名已发生
- 用户拒绝索引 DDL，可能影响查询可用性

### 4.3 `error`

表示当前阶段无法按当前输入继续推进。

典型场景：

- 目标表不存在
- 算法与需求冲突
- DDL 执行失败
- 验证 mismatch

## 5. 错误码命名规则

统一格式：

`WF-<DOMAIN>-<NNN>`

其中：

- `WF` 代表 workflow
- `<DOMAIN>` 代表问题域
- `<NNN>` 代表三位流水号

建议问题域：

- `CTX`: 上下文
- `META`: 元数据
- `INTENT`: 意图澄清
- `ALGO`: 算法
- `PROP`: 属性参数
- `NAME`: 命名
- `DDL`: 物理 DDL
- `RULE`: DistSQL / 规则
- `MODE`: 执行模式
- `LIFE`: 生命周期范围
- `VAL`: 验证

## 6. 核心错误码目录

### 6.1 上下文与元数据

- `WF-CTX-001 DATABASE_REQUIRED`
  - 场景：未提供 `database`
  - 阶段：`intaking`
  - 建议：先明确逻辑数据库

- `WF-META-001 TABLE_NOT_FOUND`
  - 场景：Proxy 逻辑视图中不存在目标表
  - 阶段：`discovering`
  - 建议：确认库名、表名是否正确

- `WF-META-002 COLUMN_NOT_FOUND`
  - 场景：逻辑表存在，但目标列不存在
  - 阶段：`discovering`
  - 建议：确认列名是否正确

- `WF-META-003 LOGICAL_METADATA_UNAVAILABLE`
  - 场景：无法从 Proxy 逻辑视图读取所需元数据
  - 阶段：`discovering`
  - 建议：检查 Proxy 连接、权限或元数据状态

### 6.2 意图与算法

- `WF-INTENT-001 INTENT_TYPE_UNCLEAR`
  - 场景：无法判定是 `encrypt` 还是 `mask`
  - 阶段：`clarifying`
  - 建议：明确说明是“加密”还是“脱敏”

- `WF-ALGO-001 ALGORITHM_NOT_FOUND`
  - 场景：用户指定的算法不在当前 Proxy 可见池中
  - 阶段：`selecting-algorithm`
  - 建议：改用推荐算法或先确认插件安装状态

- `WF-ALGO-002 ALGORITHM_CAPABILITY_CONFLICT`
  - 场景：算法不满足解密、等值查询或模糊查询需求
  - 阶段：`selecting-algorithm`
  - 建议：更换算法或调整能力诉求

- `WF-ALGO-003 CUSTOM_ALGORITHM_CAPABILITY_UNCONFIRMED`
  - 场景：自定义 SPI 算法可见，但能力位无法完整判断
  - 类型：`warning`
  - 阶段：`selecting-algorithm`
  - 建议：让用户显式确认是否接受该不确定性

### 6.3 参数与命名

- `WF-PROP-001 REQUIRED_PROPERTY_MISSING`
  - 场景：算法已确认，但缺少必填属性
  - 阶段：`collecting-properties`
  - 建议：继续补齐属性

- `WF-PROP-002 SECRET_PROPERTY_REQUIRED`
  - 场景：缺少密钥或其他敏感必填项
  - 阶段：`collecting-properties`
  - 建议：在参数采集阶段补充，不要放在默认 review 中明文展示

- `WF-NAME-001 USER_OVERRIDE_NAME_UNSAFE`
  - 场景：用户手工覆盖的列名不满足安全检查
  - 阶段：`planning-artifacts`
  - 建议：改用自动命名或重新指定名称

- `WF-NAME-002 AUTO_RENAMED_DUE_TO_CONFLICT`
  - 场景：默认派生列名发生冲突并已自动改名
  - 类型：`warning`
  - 阶段：`planning-artifacts`
  - 建议：review 时关注最终命名结果

### 6.4 执行与模式

- `WF-LIFE-001 ENCRYPT_DROP_UNSUPPORTED`
  - 场景：用户请求 `encrypt drop`
  - 阶段：`planning-artifacts`
  - 建议：告知该能力在 V1 中 deferred

- `WF-DDL-001 DDL_PERMISSION_DENIED`
  - 场景：无物理 DDL 权限
  - 阶段：`executing`
  - 建议：切换为 `manual-only` 或使用更高权限连接

- `WF-DDL-002 DDL_EXECUTION_FAILED`
  - 场景：物理列或索引 DDL 执行失败
  - 阶段：`executing`
  - 建议：返回已完成/未完成工件并允许重试

- `WF-RULE-001 RULE_EXECUTION_FAILED`
  - 场景：DistSQL 执行失败
  - 阶段：`executing`
  - 建议：返回失败语句与下一步修正建议

- `WF-MODE-001 MANUAL_EXECUTION_PENDING`
  - 场景：`manual-only` 工件已生成但用户尚未执行
  - 类型：`warning`
  - 阶段：`review`
  - 建议：等待用户手工执行完成后再触发验证

### 6.5 验证

- `WF-VAL-001 DDL_STATE_MISMATCH`
  - 场景：计划内 DDL 对象未达到预期状态
  - 阶段：`validating`
  - 建议：检查列、索引、执行顺序

- `WF-VAL-002 RULE_STATE_MISMATCH`
  - 场景：规则实际状态与计划不一致
  - 阶段：`validating`
  - 建议：检查 DistSQL 是否成功生效

- `WF-VAL-003 LOGICAL_METADATA_MISMATCH`
  - 场景：Proxy 逻辑元数据与计划不一致
  - 阶段：`validating`
  - 建议：检查元数据刷新与逻辑视图状态

- `WF-VAL-004 SQL_EXECUTABILITY_FAILED`
  - 场景：验证 SQL 无法通过 Proxy 执行链
  - 阶段：`validating`
  - 建议：检查规则、列映射与语句选择

## 7. Tool 输出约束

### 7.1 `plan_encrypt_mask_rule`

- 缺信息时，优先返回 `pending_questions`。
- 如果同时存在风险，应附带 `warning` 类型 issue。
- 不应在 plan 阶段把“待补参数”误报为执行失败。

### 7.2 `apply_encrypt_mask_rule`

- 某一步失败时，必须返回：
  - 当前失败的 `issue`
  - 已完成工件
  - 未完成工件
  - 建议后续动作

### 7.3 `validate_encrypt_mask_rule`

- mismatch 必须映射到 `WF-VAL-*` 错误码之一。
- `manual-only` 未执行前，不得输出 `passed`。

## 8. 用户可见消息规范

- `message` 面向用户，尽量简洁明确。
- `details` 面向前端、日志或调试，可更结构化。
- 含敏感参数的上下文必须打码后再进入 `message` 或 `details`。

## 9. 输出示例

### 9.1 缺失必填属性

```json
{
  "status": "planned",
  "pending_questions": [
    "请提供 AES 所需的 aes-key-value"
  ],
  "issues": [
    {
      "code": "WF-PROP-001",
      "severity": "error",
      "stage": "collecting-properties",
      "message": "当前算法仍缺少必填属性，暂时无法生成最终可执行工件。",
      "userAction": "补齐必填属性后继续。",
      "retryable": true,
      "details": {
        "missing_properties": [
          "aes-key-value"
        ]
      }
    }
  ]
}
```

### 9.2 自动改名警告

```json
{
  "status": "planned",
  "issues": [
    {
      "code": "WF-NAME-002",
      "severity": "warning",
      "stage": "planning-artifacts",
      "message": "默认派生列名发生冲突，系统已自动改名。",
      "userAction": "请在 review 中确认最终列名。",
      "retryable": false,
      "details": {
        "original_name": "phone_cipher",
        "resolved_name": "phone_cipher_1"
      }
    }
  ]
}
```

### 9.3 手工执行待完成

```json
{
  "status": "awaiting-manual-execution",
  "issues": [
    {
      "code": "WF-MODE-001",
      "severity": "warning",
      "stage": "review",
      "message": "工件已生成，但当前为 manual-only 模式，系统不会自动执行。",
      "userAction": "请手工执行后再触发验证。",
      "retryable": true,
      "details": {}
    }
  ]
}
```

# Detailed Design: Validation Matrix and Completion Criteria

## 1. 目标

这份文档把“怎么验证算完成”细化成统一矩阵，避免实现阶段把验证做成零散脚本。

V1 的验证只覆盖：

- 物理 DDL 状态
- 规则状态
- 逻辑元数据状态
- 逻辑 SQL 可执行性

V1 明确不覆盖：

- 历史数据迁移校验
- 真实结果正确性比对
- 数据回填完整性检查

## 2. 验证总原则

- 验证必须基于 Proxy 逻辑视图，而不是底层物理库直连视角。
- 每一层验证都必须输出 `passed`、`failed` 或 `skipped`。
- `manual-only` 模式下，未执行前不能直接判定成功。
- 校验失败时必须给出 mismatch 和建议动作。

## 3. 四层验证矩阵

### 3.1 DDL Validation

**目的**

- 确认计划中的物理列与索引工件是否已经落到预期状态。

**适用场景**

- encrypt create / alter
- 显式包含物理 DDL 的 mask create / alter

**证据来源**

- Proxy 可见逻辑元数据
- 必要的受控元数据查询
- 已执行工件记录

**结果判定**

- `passed`: 计划内 DDL 对象已按预期存在
- `failed`: 对象缺失、名称不符、顺序依赖未满足
- `skipped`: 本次批准计划不包含 DDL，或仍处于 manual-only 未执行前

### 3.2 Rule Validation

**目的**

- 确认 encrypt / mask 规则已按计划生效。

**适用场景**

- encrypt create / alter
- mask create / alter / drop

**证据来源**

- `SHOW ENCRYPT RULES`
- `SHOW MASK RULES`
- 当前计划中的期望规则摘要

**结果判定**

- `passed`: 规则存在且字段、算法、派生列映射与计划一致
- `failed`: 规则缺失、算法不一致、drop 后仍残留
- `skipped`: 不允许出现，规则层始终应校验

### 3.3 Logical Metadata Validation

**目的**

- 确认用户主视角下的逻辑表/列结构符合计划预期。

**适用场景**

- 所有成功执行或手工执行后的流程

**证据来源**

- Proxy 逻辑元数据查询
- 逻辑表字段清单
- 派生列规划结果

**结果判定**

- `passed`: 逻辑列仍为用户主入口，相关元数据可被 Proxy 正常解析
- `failed`: 逻辑列缺失、元数据不一致、派生结构未按预期被感知
- `skipped`: 只允许在 manual-only 且用户尚未执行时出现

### 3.4 SQL Executability Validation

**目的**

- 确认逻辑 SQL 能在 Proxy 视图下被接受并可执行。

**适用场景**

- 所有成功执行或手工执行后的流程

**证据来源**

- 受控验证 SQL
- Proxy 执行结果中的成功或错误反馈

**结果判定**

- `passed`: 代表性逻辑 SQL 可通过执行链
- `failed`: 语义或规则冲突导致 SQL 无法执行
- `skipped`: 只允许在 manual-only 且用户尚未执行时出现

## 4. 按操作类型的验证要求

### 4.1 Encrypt Create / Alter

- DDL: 视派生列与索引是否在计划内决定 `passed` 或 `skipped`
- Rule: 必须校验
- Logical Metadata: 必须校验
- SQL Executability: 必须校验

### 4.2 Mask Create / Alter

- DDL: 通常 `skipped`，除非用户批准了额外物理 DDL
- Rule: 必须校验
- Logical Metadata: 必须校验
- SQL Executability: 必须校验

### 4.3 Mask Drop

- DDL: 必须 `skipped`
- Rule: 必须校验规则已删除
- Logical Metadata: 必须校验
- SQL Executability: 必须校验

## 5. 按执行模式的验证要求

### 5.1 Auto-execute

- 执行完成后立即自动进入四层验证。

### 5.2 Review-then-execute

- 用户批准后执行。
- 执行完成后立即自动进入四层验证。

### 5.3 Manual-only

- 第一次输出时，只返回验证计划和待执行工件。
- 在用户声明“我已手工执行完成”之前：
  - Rule / Metadata / SQL Executability 可以是 `skipped`
  - 整体状态不能标记为 `passed`
- 用户触发正式验证后，再生成完整验证结果。

## 6. 验证 SQL 设计原则

- 验证 SQL 面向“可执行性”，不面向“真实业务结果正确性”。
- 优先使用最小化、低风险、只读的逻辑 SQL。
- 不要求读取真实敏感数据样本。
- 不要求比对加解密后的业务值是否正确。

## 7. mismatch 输出规范

任一层失败时，至少返回：

- `layer`
- `expected`
- `actual`
- `impact`
- `suggestedNextAction`

例如：

- 期望 `phone_cipher`，实际不存在
- 期望 encryptor 为 `AES`，实际为 `SM4`
- 期望 rule 已删除，实际 `mask rule` 仍存在

## 8. 完成标准

一次工作流可被标记为 `completed`，必须满足以下其一：

### 8.1 自动或审批后执行路径

- 所有必须执行的工件已经完成
- 四层验证都已给出最终结果
- 所有非适用项被正确标记为 `skipped`

### 8.2 手工路径

- MCP 已交付完整工件与验证步骤
- 用户明确声明手工执行完成
- MCP 已完成后置验证并返回结果

如果仅仅生成了工件但用户尚未执行，只能算：

- `awaiting-manual-execution`

不能算：

- `completed`

## 9. 实现约束

- 不允许把 DDL 成功等同于整体成功。
- 不允许跳过规则验证。
- 不允许在 `manual-only` 未执行前输出成功结论。
- 不允许用真实数据正确性检查替代 SQL 可执行性验证。

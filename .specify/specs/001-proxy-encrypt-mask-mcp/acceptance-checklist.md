# Acceptance Checklist: ShardingSphere-Proxy Encrypt and Mask MCP V1

## 1. 使用方式

这份清单用于实现完成后的验收。

建议每一项都记录三类信息：

- `Result`: pass / fail / skipped
- `Evidence`: 命令、响应摘要、日志或截图
- `Notes`: 异常说明或补充上下文

## 2. 基础范围验收

- [ ] 仅支持 `ShardingSphere-Proxy`
- [ ] MCP 运行时连接的是 Proxy，而不是底层物理库
- [ ] 所有工作流都要求显式 `database`
- [ ] 工作流主视角始终是逻辑库、逻辑表、逻辑列
- [ ] encrypt / mask 都支持 `create / alter / drop`
- [ ] V1 不处理历史数据迁移或回填
- [ ] V1 不要求回滚
- [ ] V1 不要求审计落库

## 3. 交互流程验收

- [ ] 任意请求一开始都会返回全局步骤清单
- [ ] `all-at-once` 模式会连续推进，直到遇到补信息或审批点
- [ ] `step-by-step` 模式会在关键步骤后暂停
- [ ] `step-by-step` 模式下，系统能在当前服务运行期内记住已确认上下文
- [ ] 服务重启后不要求恢复 `step-by-step` 上下文
- [ ] review 前不会直接执行 SQL 或 DistSQL
- [ ] `delivery_mode` 会真实影响暂停点与续做语义，而不是仅仅回显

## 4. 输入职责边界与追问验收

- [ ] MCP 优先使用上游传入的结构化意图字段
- [ ] 原始自然语言只作为补充上下文，而不是唯一规则输入
- [ ] 信息不完整时会持续追问，而不是猜测执行
- [ ] 加密场景会追问解密、等值查询、模糊查询等规则相关需求
- [ ] 脱敏场景会追问字段语义和展示要求
- [ ] 不会把“强语义自然语言理解”做成 MCP 的硬责任

## 5. 算法推荐与参数采集验收

- [ ] 推荐池覆盖内置算法与当前 Proxy 可见的自定义 SPI 算法
- [ ] `show plugins` 只是发现入口，不会被误当成完整能力判断
- [ ] 算法确定前不会要求输入全量参数
- [ ] 算法确定后，系统会继续追问必填属性
- [ ] 敏感参数在 review 中默认打码
- [ ] 自定义 SPI 能力不完整时，会给 warning，而不是静默假定支持

## 6. 命名与工件规划验收

- [ ] 默认派生列名为 `*_cipher`、`*_assisted_query`、`*_like_query`
- [ ] 默认不会自动复用同名旧物理列
- [ ] 命名冲突时会自动加数字后缀
- [ ] 自动改名后会把最终名称回传给用户
- [ ] 生成的物理列类型沿用 ShardingSphere 默认策略
- [ ] 需要查询能力时会生成索引建议或索引 DDL
- [ ] encrypt alter / drop 不会生成 cleanup DDL，物理清理由用户自行处理

## 7. 执行模式验收

### 7.1 Auto-execute

- [ ] review 后系统可自动执行已批准工件
- [ ] 执行顺序正确反映 DDL、索引、DistSQL 依赖
- [ ] 每一步都有进度反馈

### 7.2 Review-then-execute

- [ ] 用户审批前，系统只展示工件不执行
- [ ] 审批后按预期顺序执行

### 7.3 Manual-only

- [ ] 不会自动执行任何 SQL 或 DistSQL
- [ ] 默认 review 只展示打码后的敏感参数
- [ ] 用户明确要求时，系统可返回 executable artifact package
- [ ] 未手工执行前，系统状态保持 `awaiting-manual-execution`

## 8. 生命周期验收

### 8.1 Encrypt Create / Alter

- [ ] 能生成 encrypt planning 工件
- [ ] 能执行或导出对应 DDL / DistSQL
- [ ] alter 流会先读取现有规则，不会盲改
- [ ] 收缩式 alter 会明确说明 MCP 不处理遗留物理工件 cleanup

### 8.2 Encrypt Drop

- [ ] 会先读取当前 encrypt 规则并确认目标存在
- [ ] 能生成 `DROP ENCRYPT RULE` 工件
- [ ] 会明确说明“不恢复历史明文数据”
- [ ] 会明确说明遗留物理工件 cleanup 由用户自行处理
- [ ] 删除后仍会进入验证与总结

### 8.3 Mask Create / Alter

- [ ] 能生成 mask rule-only planning 工件
- [ ] alter 流会先读取现有规则，不会盲改

### 8.4 Mask Drop

- [ ] 仅删除 mask 规则
- [ ] 不会生成物理清理 DDL
- [ ] 删除后仍会进入验证与总结

## 9. 错误语义与边界验收

- [ ] warning / error 都带稳定错误码
- [ ] warning / error 都包含 stage、retryable、suggested next action
- [ ] 缺少数据库时返回 `WF-CTX-001`
- [ ] 表不存在时返回 `WF-META-001`
- [ ] 列不存在时返回 `WF-META-002`
- [ ] 算法不存在时返回 `WF-ALGO-001`
- [ ] 算法能力冲突时返回 `WF-ALGO-002`
- [ ] 自定义 SPI 能力不完整时返回 `WF-ALGO-003`
- [ ] 缺少必填属性时返回 `WF-PROP-001`
- [ ] 自动改名时返回 `WF-NAME-002`
- [ ] drop 目标规则不存在时返回 `WF-LIFE-001`
- [ ] DDL 权限不足时返回 `WF-DDL-001`
- [ ] 验证 mismatch 时返回 `WF-VAL-*`

## 10. 四层验证验收

- [ ] encrypt create / alter 完成后会校验 DDL、Rule、Logical Metadata、SQL Executability
- [ ] encrypt drop 完成后会校验 Rule、Logical Metadata、SQL Executability
- [ ] mask create / alter 完成后至少校验 Rule、Logical Metadata、SQL Executability
- [ ] mask drop 会校验规则已删除
- [ ] `manual-only` 未执行前不会误报 `passed`
- [ ] mismatch 会返回 expected / actual / impact / suggestedNextAction

## 11. 数据边界与安全验收

- [ ] 完成标准不依赖样本数据
- [ ] 默认流程不读取样本数据
- [ ] 敏感参数不会在默认 summary 中明文出现
- [ ] encrypt drop 只承诺规则 / 元数据 / SQL 可执行性范围内的验证

## 12. 典型会话验收矩阵

- [ ] `conversation-examples.md` 示例一可跑通
- [ ] `conversation-examples.md` 示例二可跑通
- [ ] `conversation-examples.md` 示例三可跑通
- [ ] `conversation-examples.md` 示例四可跑通
- [ ] `conversation-examples.md` 示例五可跑通
- [ ] `conversation-examples.md` 示例六可跑通
- [ ] `conversation-examples.md` 示例七可跑通
- [ ] `conversation-examples.md` 示例八可跑通

## 13. 实现质量验收

- [ ] `mcp/core` 相关单测通过
- [ ] 相关 Checkstyle / Spotless 检查通过
- [ ] 新增 README 或契约文档与实现一致
- [ ] 没有引入与本规格冲突的额外行为

## 14. 发布前最终结论

只有以下条件同时满足，V1 才可判定为可交付：

- [ ] 核心场景通过
- [ ] 三种执行模式通过
- [ ] 四层验证通过
- [ ] 错误码语义通过
- [ ] 边界与不支持能力行为符合规格

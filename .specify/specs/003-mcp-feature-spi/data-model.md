# Data Model: MCP Feature SPI Modularization

## 1. 建模目标

- 模型要表达“谁拥有 surface”，而不是只表达“有哪些 handlers”。
- 模型要支持 core 与 feature 解耦，同时保证 registry 聚合仍可确定执行。
- 模型要覆盖 future feature 扩展，而不只服务 encrypt / mask 两个现有模块。
- 模型要把 tool naming family、resource namespace、workflow state ownership 一起显式化。

## 2. 核心实体

### 2.1 FeatureModule

一个位于 `mcp/features` 下的 MCP 业务能力模块。

- `moduleName`: 模块名，例如 `encrypt`、`mask`
- `toolHandlers`: 当前 feature 模块通过 SPI 注册的 tool handlers
- `resourceHandlers`: 当前 feature 模块通过 SPI 注册的 resource handlers
- `workflowImplementations`: 当前 feature 对外需要稳定暴露的 planner / applier / validator 等实现

**Validation rules**

- `moduleName` 必须非空
- `toolHandlers` 和 `resourceHandlers` 不能同时为空
- 所有 surface 的命名与 URI namespace 必须和模块 ownership 一致

### 2.2 ToolHandler Contribution

一个由 feature 模块通过 SPI 暴露给 registry 的 MCP tool surface。

- `name`: tool 名称
- `descriptor`: MCP tool descriptor
- `owningModule`: 所属 feature 模块
- `handler`: 实际执行逻辑

**Validation rules**

- `name` 必须非空
- `name` 在全局 registry 级别必须唯一
- `descriptor` 必须完整，required field 与 schema 定义保持一致

### 2.3 ResourceHandler Contribution

一个由 feature 模块通过 SPI 暴露给 registry 的 MCP resource surface。

- `uriPattern`: resource URI pattern
- `owningModule`: 所属 feature 模块
- `handler`: 实际读取逻辑

**Validation rules**

- `uriPattern` 必须非空
- `uriPattern` 在全局 registry 级别必须唯一
- 任意两个 pattern 不能 overlap

### 2.4 MCPFeatureRuntimeContext

feature handler 通过 `mcp/features/spi` 看到的共享运行时 facade，而不是 core implementation。

- `metadataFacade`: 逻辑元数据查询能力
- `executionFacade`: SQL / DistSQL 执行能力
- `sessionFacade`: session 与事务相关能力
- `workflowStore`: feature workflow 状态读写能力
- `capabilityFacade`: 数据库 / 服务 capability 查询能力

**Validation rules**

- facade 暴露共享能力，但不泄露 `mcp/core` 实现细节
- feature handler 只能通过这些 facade 与上层 runtime 交互

### 2.5 FeatureWorkflowSnapshot

某个 feature 自己管理的 workflow 状态对象。

- `planId`: 计划标识
- `sessionId`: 创建该 workflow 的 session
- `owningModule`: feature ownership
- `status`: 当前状态
- `payload`: feature-specific planning / review / execute / validate state

**Validation rules**

- `planId` 全局唯一
- `owningModule` 必须与具体 feature 模块一致
- `payload` 可以 feature-specific，但 store 与 registry 不得依赖其内部结构

### 2.6 FeatureSurfaceCatalog

`mcp/core` 聚合完成后的最终可见 surface 目录。

- `tools`: 已注册 tool map
- `resources`: 已注册 resource map
- `supportedModules`: 当前 runtime 实际启用的 feature 模块列表

**Validation rules**

- 聚合顺序必须确定
- 单个 feature 缺失时，不影响其他 feature 和 core surface
- catalog 构建失败时必须给出显式错误来源

### 2.7 FeatureContractNamespace

某个 feature 的首发外部 contract 约束。

- `toolNameFamily`: 例如 `plan_encrypt_rule` / `apply_encrypt_rule` / `validate_encrypt_rule`
- `resourceNamespace`: 例如 `shardingsphere://features/encrypt/...`
- `owningModule`: 对应 feature 模块

**Validation rules**

- tool 与 URI namespace 必须和 feature ownership 一致
- 不允许多个 feature 共享同一组外部 contract 名称

## 3. 实体关系

- 一个 `FeatureModule` 拥有多个 `ToolHandler Contribution`
- 一个 `FeatureModule` 拥有多个 `ResourceHandler Contribution`
- 一个 `FeatureModule` 生成并管理自己的 `FeatureWorkflowSnapshot`
- `FeatureSurfaceCatalog` 聚合多个 feature modules 提供的 surface
- 每个 feature module 必须绑定一个 `FeatureContractNamespace`

## 4. 状态与生命周期

### 4.1 Handler SPI lifecycle

1. handler 通过 SPI 被发现
2. registry 校验 tool / resource surface 完整性与唯一性
3. registry 聚合 tools / resources
4. bootstrap 基于 catalog 暴露 MCP tool / resource specifications

### 4.2 Workflow lifecycle

1. feature-specific `plan_*` tool 创建或更新 `FeatureWorkflowSnapshot`
2. feature-specific `apply_*` tool 读取同一 snapshot 并推进执行状态
3. feature-specific `validate_*` tool 读取同一 snapshot 并完成验证

`status` 的具体取值由 feature 决定，但 encrypt / mask 当前仍沿用 001 已定义的 planning / review / execute / validate 语义。

## 5. 首发实例化结果

### 5.1 Encrypt

- `owningModule`: `encrypt`
- `toolNameFamily`:
  - `plan_encrypt_rule`
  - `apply_encrypt_rule`
  - `validate_encrypt_rule`
- `resourceNamespace`:
  - `shardingsphere://features/encrypt/algorithms`
  - `shardingsphere://features/encrypt/databases/{database}/rules`
  - `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`

### 5.2 Mask

- `owningModule`: `mask`
- `toolNameFamily`:
  - `plan_mask_rule`
  - `apply_mask_rule`
  - `validate_mask_rule`
- `resourceNamespace`:
  - `shardingsphere://features/mask/algorithms`
  - `shardingsphere://features/mask/databases/{database}/rules`
  - `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

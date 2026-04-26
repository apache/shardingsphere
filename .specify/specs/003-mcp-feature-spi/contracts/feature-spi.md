# Contract: MCP Feature SPI

## 1. 设计目标

- `mcp/features/spi` 是 feature 模块和上层 MCP 基础设施之间唯一稳定边界。
- `mcp/core` 不允许直接依赖 encrypt / mask 实现类。
- feature 通过 handler SPI 被发现，通过 SPI 合约暴露 surface，通过 SPI runtime facade 访问共享能力。

## 2. 顶层 SPI

### 2.1 `ToolHandler`

当前 MCP tool handler contract 应提升到 `mcp/features/spi`，并成为 feature tool surface 的唯一 SPI 注册入口。

保留能力：

- 描述 tool schema
- 处理一次 tool call
- 通过 SPI runtime facade 访问共享能力

约束：

- tool name 全局唯一
- tool handler 不得通过 core implementation class 直接访问共享能力
- feature module 必须通过 `META-INF/services/org.apache.shardingsphere.mcp.tool.handler.ToolHandler` 注册自己的 tool handlers

### 2.2 `ResourceHandler`

当前 MCP resource handler contract 应提升到 `mcp/features/spi`，并成为 feature resource surface 的唯一 SPI 注册入口。

保留能力：

- 声明 URI pattern
- 处理一次 resource read
- 通过 SPI runtime facade 访问共享能力

约束：

- URI pattern 全局唯一
- 任意两个 pattern 不允许 overlap
- feature module 必须通过 `META-INF/services/org.apache.shardingsphere.mcp.resource.handler.ResourceHandler` 注册自己的 resource handlers

## 3. Runtime facade contracts

feature handler 不应直接拿到 `mcp/core` implementation，而应拿到窄接口 facade。

建议至少包含以下 facade：

```java
public interface MCPFeatureRuntimeContext {

    MCPFeatureMetadataFacade getMetadataFacade();

    MCPFeatureExecutionFacade getExecutionFacade();

    MCPFeatureSessionFacade getSessionFacade();

    MCPFeatureWorkflowStore getWorkflowStore();

    MCPFeatureCapabilityFacade getCapabilityFacade();
}
```

说明：

- `MetadataFacade` 负责逻辑元数据读取
- `ExecutionFacade` 负责 SQL / DistSQL 执行
- `SessionFacade` 负责 session 级上下文
- `WorkflowStore` 负责多步工作流状态续接
- `CapabilityFacade` 负责数据库或服务 capability 查询

这些 facade 的实现留在 `mcp/core`，但 contract 属于 `mcp/features/spi`。

## 4. Optional workflow subcontracts

为了保持 `spi` 足够纯，但又不把所有 helper 都公开，建议只提升少量稳定 workflow seam：

```java
public interface MCPFeaturePlanner {

    Object plan(MCPFeatureRuntimeContext runtimeContext, String sessionId, Map<String, Object> arguments);
}

public interface MCPFeatureApplier {

    Object apply(MCPFeatureRuntimeContext runtimeContext, String sessionId, Map<String, Object> arguments);
}

public interface MCPFeatureValidator {

    Object validate(MCPFeatureRuntimeContext runtimeContext, String sessionId, Map<String, Object> arguments);
}
```

说明：

- encrypt / mask 可以各自实现这三类 contract
- recommendation、template、naming、rule inspection 等 helper 默认留在 feature 内部，不进入公共 SPI

## 5. Tool naming contract

### 5.1 Encrypt

- `plan_encrypt_rule`
- `apply_encrypt_rule`
- `validate_encrypt_rule`

### 5.2 Mask

- `plan_mask_rule`
- `apply_mask_rule`
- `validate_mask_rule`

约束：

- 不再保留 `*_encrypt_mask_*` 共享命名
- 后续新 feature 也遵循 `plan_<feature>_rule`、`apply_<feature>_rule`、`validate_<feature>_rule`

## 6. Resource URI contract

### 6.1 Encrypt

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`

### 6.2 Mask

- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

约束：

- URI 首段必须体现 feature ownership
- 不保留旧的平铺式命名作为兼容层

## 7. Registry assembly contract

`mcp/core` 的 registry 装配流程应满足：

1. 加载所有 `ToolHandler`
2. 加载所有 `ResourceHandler`
3. 校验 duplicate tool name
4. 校验 duplicate / overlapping URI pattern
5. 构建最终可见 surface catalog

失败策略：

- 任意冲突都在启动期显式失败
- 不允许 silent shadowing
- 缺失或无效 SPI 注册必须给出明确错误来源

## 8. Dependency contract

必须满足：

- `mcp/core -> mcp/features/spi`
- `mcp/features/encrypt -> mcp/features/spi`
- `mcp/features/mask -> mcp/features/spi`

不得出现：

- `mcp/core -> mcp/features/encrypt`
- `mcp/core -> mcp/features/mask`
- `mcp/features/encrypt -> mcp/core`
- `mcp/features/mask -> mcp/core`

`mcp/bootstrap` 只负责把官方 feature jars 带入运行时，不直接引用具体实现类。

## 9. Feature metadata note

本次 feature modularization 不要求顶层 feature metadata SPI 参与 tool / resource surface 装配。

如果未来需要 feature catalog、display name、feature capability summary 等额外 metadata：

- 应定义为单独的可选 SPI
- 不应成为 tool / resource registry assembly 的前置条件
- 不应改变本次已经确定的 handler-level pluginization 模型

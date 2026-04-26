# Quickstart: MCP Feature SPI Modularization

## 1. 目标

这份 quickstart 不是实现代码，而是后续落地时判断“插件化是否真的成立”的验收样板。

只要下面这些步骤能成立，就说明 `mcp/features/spi`、`encrypt`、`mask`、`core`、`bootstrap` 的职责拆分是对的。

## 2. 目标模块结构

```text
mcp
|-- features
|   |-- spi
|   |-- encrypt
|   `-- mask
|-- core
`-- bootstrap
```

## 3. 预期依赖方向

- `mcp/core -> mcp/features/spi`
- `mcp/features/encrypt -> mcp/features/spi`
- `mcp/features/mask -> mcp/features/spi`
- `mcp/bootstrap -> mcp/core`
- `mcp/bootstrap` 在运行时携带 encrypt / mask jars，但不直接引用其实现类

反例：

- `mcp/core -> mcp/features/encrypt`
- `mcp/core -> mcp/features/mask`
- `mcp/features/encrypt -> mcp/core`
- `mcp/features/mask -> mcp/core`

这些依赖一旦出现，就说明插件化边界已经破坏。

## 4. Encrypt 模块验收样板

### 4.1 Encrypt SPI 注册

- `mcp/features/encrypt` 自己注册 `ToolHandler`
- `mcp/features/encrypt` 自己注册 `ResourceHandler`
- `mcp/core` 不直接引用 encrypt 实现类

### 4.2 Encrypt 首发 tool family

- `plan_encrypt_rule`
- `apply_encrypt_rule`
- `validate_encrypt_rule`

不应再出现：

- `plan_encrypt_mask_rule`
- `apply_encrypt_mask_rule`
- `validate_encrypt_mask_rule`

### 4.3 Encrypt 首发资源空间

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`

### 4.4 Encrypt workflow ownership

以下能力都应位于 `mcp/features/encrypt`：

- rule inspection
- algorithm recommendation
- property template
- derived column / index planning
- DistSQL planning
- apply / validate workflow

## 5. Mask 模块验收样板

### 5.1 Mask SPI 注册

- `mcp/features/mask` 自己注册 `ToolHandler`
- `mcp/features/mask` 自己注册 `ResourceHandler`
- `mcp/core` 不直接引用 mask 实现类

### 5.2 Mask 首发 tool family

- `plan_mask_rule`
- `apply_mask_rule`
- `validate_mask_rule`

### 5.3 Mask 首发资源空间

- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

### 5.4 Mask workflow ownership

以下能力都应位于 `mcp/features/mask`：

- rule inspection
- algorithm recommendation
- property template
- DistSQL planning
- apply / validate workflow

## 6. 单 feature 装载场景

### 场景 A：只装载 encrypt

预期结果：

- encrypt tools / resources 可见
- mask tools / resources 不可见
- core 自己的 metadata / execute / capability surface 仍然可见
- 启动不因 mask 缺失而失败

### 场景 B：只装载 mask

预期结果：

- mask tools / resources 可见
- encrypt tools / resources 不可见
- core 自己的 metadata / execute / capability surface 仍然可见
- 启动不因 encrypt 缺失而失败

## 7. 冲突场景

### 场景 A：两个 feature 暴露了同名 tool

预期结果：

- registry 启动失败
- 错误明确指出冲突 tool name 和两个来源

### 场景 B：两个 feature 暴露了重复或重叠 URI

预期结果：

- registry 启动失败
- 错误明确指出冲突 URI pattern 和两个来源

### 场景 C：feature jar 在 classpath 中，但缺失应有的 handler SPI 注册

预期结果：

- 对应 feature surface 不会被静默部分发布
- 缺失或不完整的 SPI 注册有明确错误或明确缺失结果

## 8. 新增未来 feature 的标准步骤

以 `audit` 为例：

1. 新建 `mcp/features/audit`
2. 依赖 `mcp/features/spi`
3. 实现自己的 `ToolHandler`
4. 实现自己的 `ResourceHandler`
5. 暴露自己的 tool family，例如 `plan_audit_rule`
6. 暴露自己的 resource namespace，例如 `shardingsphere://features/audit/...`
7. 在 `META-INF/services` 注册 handler
8. 将 jar 放入 MCP runtime classpath

如果以上步骤无需修改 `mcp/core` 业务分支，就说明本次 SPI 设计达到了目标。

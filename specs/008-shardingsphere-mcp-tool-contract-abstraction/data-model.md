# Data Model: ShardingSphere MCP Tool Contract Abstraction Between Core and Bootstrap

## Core Domain Entities

### ToolDescriptor

- **Purpose**: 描述一个 MCP tool 的完整静态契约。
- **Fields**:
  - `name`
  - `title`
  - `description`
  - `dispatchKind`
  - `inputDefinition`
- **Validation rules**:
  - `name` 在 supported tool 集合中必须唯一。
  - `title` 必须稳定，对外呈现规则不能因本轮重构改变。
  - `description` 不能为空，且表达的是 tool 本身，而不是 transport 实现细节。
  - `dispatchKind` 与运行时分发路径一一对应。
  - `inputDefinition` 对每个 supported tool 都必须显式存在，包括零参数 tool。

### ToolDispatchKind

- **Purpose**: 描述一个 tool 在运行时属于哪类分发路径。
- **Values**:
  - `METADATA`
  - `CAPABILITY`
  - `EXECUTION`
- **Validation rules**:
  - 每个 tool 必须且只能选择一个 `dispatchKind`。
  - `dispatchKind` 只表达运行时分类，不携带 transport 细节。

### ToolInputDefinition

- **Purpose**: 描述 tool 输入根对象的抽象定义。
- **Fields**:
  - `fields`
  - `allowsAdditionalProperties`
- **Validation rules**:
  - `fields` 保持稳定顺序，以便 `tools/list` 输出顺序可预测。
  - `fields` 可以为空，但空字段列表只允许用于显式零参数 tool。
  - `allowsAdditionalProperties` 必须显式保存当前行为，不依赖 adapter 的隐式默认值。

### ToolFieldDefinition

- **Purpose**: 描述输入根对象中的一个字段。
- **Fields**:
  - `name`
  - `valueDefinition`
  - `required`
- **Validation rules**:
  - `name` 在同一 `ToolInputDefinition` 中必须唯一。
  - `required` 直接描述字段是否必填，不再由外部列表隐式拼接。
  - `valueDefinition` 不能为空。

### ToolValueDefinition

- **Purpose**: 描述字段值的抽象类型定义。
- **Variants**:
  - `ToolScalarDefinition`
  - `ToolArrayDefinition`
- **Validation rules**:
  - 变体必须足以表达当前 MCP tool 输入模型，不引入无使用场景的额外层级。

### ToolScalarDefinition

- **Purpose**: 描述 string、integer 等标量字段。
- **Fields**:
  - `type`
  - `description`
  - `allowedValues`
- **Validation rules**:
  - 当前至少需要覆盖 `STRING` 与 `INTEGER`。
  - `allowedValues` 为可选约束，用于未来像 `object_type` 这样的有限集合表达；
    本轮即使暂不全部启用，也应为模型保留同层能力。

### ToolArrayDefinition

- **Purpose**: 描述数组字段。
- **Fields**:
  - `description`
  - `itemDefinition`
- **Validation rules**:
  - `itemDefinition` 不能为空。
  - 当前 V1 主要覆盖 array-of-scalar 场景，如 `object_types`。
  - 嵌套 array 或复杂 object items 不属于本轮必须能力。

## Relationships

- `ToolDescriptor.inputDefinition` 持有一个 `ToolInputDefinition`。
- `ToolInputDefinition.fields` 由多个 `ToolFieldDefinition` 组成，并保持稳定顺序。
- `ToolFieldDefinition.valueDefinition` 指向一个 `ToolValueDefinition` 变体。
- `ToolDispatchKind` 决定 `ToolDescriptor` 在 runtime 中走 metadata、capability
  或 execution 路径。
- bootstrap adapter 从 `ToolInputDefinition` 出发，生成 SDK 所需的 root object schema、
  ordered properties 与 required 列表。

## Canonical Descriptor Shape

```text
ToolDescriptor
├── name = "get_capabilities"
├── title = "Get Capabilities"
├── description = "ShardingSphere MCP tool: get_capabilities"
├── dispatchKind = CAPABILITY
└── inputDefinition
    └── fields
        └── ToolFieldDefinition
            ├── name = "database"
            ├── required = false
            └── valueDefinition = ToolScalarDefinition(STRING)
```

## Explicit Coverage Reference

### Zero-argument tool

```text
ToolDescriptor(name = "list_databases")
  inputDefinition.fields = []
```

Expected behavior:

- 该 tool 仍然有显式 `ToolInputDefinition`。
- bootstrap 不再通过 unknown-tool fallback 推断其为空对象 schema。

### Optional-argument tool

```text
ToolDescriptor(name = "get_capabilities")
  inputDefinition.fields = [database(required = false)]
```

Expected behavior:

- `tools/list` 能显式暴露可选 `database` 字段。
- runtime 仍允许无参调用服务级 capability。

### Array-valued tool field

```text
ToolDescriptor(name = "search_metadata")
  inputDefinition.fields includes object_types(array<string>, required = false)
```

Expected behavior:

- core descriptor 能稳定表达 `object_types`。
- bootstrap adapter 递归生成 array item schema，而不是写死在 tool-specific branch 中。

## State Transitions

- `core_descriptor_defined -> sdk_schema_adapted`
  - bootstrap adapter 把 `ToolInputDefinition` 转成 `McpSchema.JsonSchema`。
- `sdk_schema_adapted -> tools_list_exposed`
  - `MCPToolSpecificationFactory` 基于 descriptor 生成 `SyncToolSpecification`。
- `raw_tool_arguments -> normalized_core_request`
  - `MCPToolCatalog` 继续把输入参数归一化成 `ToolRequest` 或 `ExecutionRequest`。
- `descriptor_lookup_missing -> rejected`
  - 未知 tool 不应再通过空 schema 被“描述成功”，而应继续走当前 unsupported / invalid 路径。

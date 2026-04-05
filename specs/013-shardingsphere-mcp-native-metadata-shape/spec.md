# Feature Specification: ShardingSphere MCP Native Metadata Shape Without MetadataObject Flattening

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-05  
**Status**: Design draft  
**Input**: User description:
"MetadataObject过度设计了。我就希望数据库里是什么样子，展现就是什么样子。比如说库schema表列索引，你为什么要过度设计，过度抽象呢？你就把原来的样子展现出来吧。用speckit设计我的需求，但不允许切换分支"

## Scope Statement

本 follow-up 只处理 `mcp/core` metadata 主链路与紧邻测试中的模型和展现方式：

- `MetadataObject` 不再作为 metadata snapshot、query 和 response 的源模型
- runtime metadata 改为保留数据库原生层级：database -> schema -> table/view -> column/index
- metadata resource 与 metadata tool 改为输出贴近数据库原貌的 typed payload，而不是统一扁平对象
- 保持现有 resource URI 与 metadata tool name 不变
- capability 与 `execute_query` surface 不在本轮调整范围内
- 本轮只允许修改 metadata 相关生产代码、测试代码和 Speckit 文档，不允许夹带无关小修小改

本特性的核心不是给 `MetadataObject` 换个名字，
而是把 “数据库天然结构” 重新拉回 metadata 设计的中心。

## Problem Statement

当前 metadata 链路的根问题不是字段不够，而是源模型方向错了。

已知现状包括：

- `MCPJdbcMetadataLoader` 从 JDBC `DatabaseMetaData` 读取到的是 database / schema / table / view /
  column / index 的天然层级
- 这些结果一进入 runtime snapshot 就被压平成 `MetadataObject`
- `DatabaseMetadataSnapshot` 与 `DatabaseMetadataSnapshots` 持有的是 `MetadataObject` 集合与全局平铺列表
- `MetadataQueryService` 通过 `objectType + schema + parentObjectType + parentObjectName`
  在平铺列表上过滤，而不是沿真实层级导航
- `MCPMetadataResponse`、metadata resources、metadata tools 最终对外暴露的也是同一类 generic item

这会带来五个直接问题：

1. database 结构被丢失，只能靠 `parentObjectType` / `parentObjectName` 事后补关系  
2. table、view、column、index 的对象边界被抹平，只剩一套 generic 字段  
3. `describe_table` / `describe_view` 这类 detail 结果需要从多条平面记录重新拼装  
4. query 路径围绕“过滤一堆对象”设计，而不是围绕“读取某个真实元数据节点”设计  
5. 以后如果要保留更贴近数据库原貌的字段，只能继续给 generic 对象打补丁

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Resource 返回数据库原貌结构 (Priority: P1)

作为 MCP metadata 使用者，我希望 resource 读取到的就是数据库对象本来的结构，
这样我看到 schema、table、view、column、index 时，不需要再去理解一个人为设计的统一平面模型。

**Why this priority**: 这是用户明确提出的第一目标；如果 resource 结果仍然是 `MetadataObject`，
这轮设计就没有真正解决问题。

**Independent Test**: resource handler 与 controller dedicated tests 可以独立验证 list/detail resource
 返回 typed metadata payload，而不是 `MetadataObject` 列表。

**Acceptance Scenarios**:

1. **Given** 请求 `shardingsphere://databases/logic_db/schemas/public/tables/orders`，
   **When** 读取 resource，**Then** 返回必须是 table-shaped payload，包含 table 自身信息和其直接子节点，
   而不是一条 table 记录再加若干 sibling `MetadataObject`。
2. **Given** 请求 `shardingsphere://databases/logic_db/schemas/public/tables/orders/columns`，
   **When** 读取 resource，**Then** 返回必须是 column objects 列表，column 与 table 的关系由层级语义表达，
   而不是 `parentObjectType=TABLE` / `parentObjectName=orders`。
3. **Given** 当前数据库类型不支持 index resource，
   **When** 请求 `/indexes` 或 `/indexes/{index}`，**Then** 仍然返回当前 `unsupported` 语义，
   不能因为模型重做而漂移。

---

### User Story 2 - Metadata tool 返回 typed metadata，而不是 generic item (Priority: P1)

作为 MCP tool 使用者，我希望 metadata tool 的结果与所请求的对象类型一致，
而不是无论 `list_tables`、`list_columns`、`describe_table` 都走同一个 `MetadataObject` 通道。

**Why this priority**: `MetadataObject` 不只影响 resource，
也已经成为 metadata tool 的统一输出模型；只改一半会留下同样的抽象问题。

**Independent Test**: `MetadataToolDispatcherTest` 与 `MCPToolPayloadResolverTest`
 可独立验证 list / describe / search 三类 metadata tool 的输出形状。

**Acceptance Scenarios**:

1. **Given** 调用 `describe_table(database, schema, table)`，
   **When** tool dispatch 完成，**Then** 返回必须是一个 table detail object，
   并直接包含 columns 与 indexes，而不是 “table object + column objects” 的平铺拼接。
2. **Given** 调用 `list_columns(database, schema, object_type, object_name)`，
   **When** tool dispatch 完成，**Then** 返回必须是 column objects 列表，
   不再暴露 `parentObjectType` / `parentObjectName`。
3. **Given** 调用 `search_metadata(...)` 搜索多种 object type，
   **When** tool dispatch 完成，**Then** 可以返回 purpose-built 的 search summary objects，
   但该 summary 只能是搜索结果投影，不能反过来变成 runtime metadata 的源模型。

---

### User Story 3 - Runtime snapshot 和 query 回到真实层级 (Priority: P1)

作为 MCP 维护者，我希望 loader、snapshot 与 query service 围绕真实 metadata 层级工作，
这样后续扩展字段、做 detail 查询或处理数据库差异时，都不需要先 flatten 再 reconstruct。

**Why this priority**: 只改 response 但保留 flat source model，
会让过度抽象继续留在系统内部，问题只是被藏起来而没有被解决。

**Independent Test**: `MCPJdbcMetadataLoaderTest` 与 `MetadataQueryServiceTest`
 可独立验证 native hierarchy 建模和按路径导航查询。

**Acceptance Scenarios**:

1. **Given** JDBC metadata loader 读取到 schema、table、view、column、index，
   **When** 构建 runtime snapshot，**Then** snapshot 必须保留明确的 ownership 层级，
   而不是立刻变成 `List<MetadataObject>`。
2. **Given** 查询某个 schema 下的 tables，
   **When** metadata query 执行，**Then** 系统必须沿 database -> schema -> tables 导航，
   而不是在全局平铺列表上做 object type 过滤。
3. **Given** 某个数据库没有显式 schema 或 schema 为空，
   **When** snapshot 与 query 执行，**Then** 系统必须保留该数据库的真实语义，
   不能为了适配 generic 模型而补造虚假的 parent 结构。

### Edge Cases

- `search_metadata` 跨 object type 搜索时需要异构结果摘要，但这个摘要不能反客为主变成 canonical model。
- `describe_table` 需要同时覆盖 table 自身、columns 与 indexes；`describe_view` 需要覆盖 view 与 columns。
- schema 为空、默认 schema 或数据库无 schema 概念时，必须反映真实数据库语义。
- index 继续是 capability-gated optional object；不支持时仍返回 `unsupported`。
- list / search 的分页能力需要保留，但分页 envelope 不应再强绑定 `MetadataObject`。
- 本轮不是为了暴露每个数据库 vendor 的所有底层字段；重点是先把结构和对象边界拉正。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 以数据库原生 metadata 层级作为 runtime source of truth，
  `MetadataObject` MUST NOT 继续充当 metadata snapshot、query 与 response 的 canonical model。
- **FR-002**: `MCPJdbcMetadataLoader` MUST 直接构建 typed metadata hierarchy，
  至少覆盖 database、schema、table、view、column、index 六类对象。
- **FR-003**: `DatabaseMetadataSnapshot` 与 `DatabaseMetadataSnapshots` MUST 持有 native hierarchy，
  MUST NOT 继续以全局 `Collection<MetadataObject>` 或等价平铺列表作为主要存储形态。
- **FR-004**: table / view / column / index 的归属关系 MUST 通过对象层级表达，
  而不是依赖 `parentObjectType` 与 `parentObjectName` 字段补充。
- **FR-005**: metadata query service MUST 通过层级导航完成 database、schema、table、view、
  column、index 的 list / detail 查询；基于全局平铺列表的主路径过滤 SHOULD 被移除。
- **FR-006**: metadata resource 的返回值 MUST 改为贴近所请求对象类型的 typed payload。
  list resource 可以保留 `items` envelope，但 `items` 内容 MUST 不再是 `MetadataObject`。
- **FR-007**: metadata tool 的 list 系列结果 MUST 返回 typed metadata objects；
  `describe_table` MUST 返回 table detail object，
  `describe_view` MUST 返回 view detail object。
- **FR-008**: `describe_table` 的 detail payload MUST 直接包含 columns 与 indexes；
  `describe_view` 的 detail payload MUST 直接包含 columns。
- **FR-009**: `search_metadata` MUST 使用 dedicated search summary model 或等价结果投影，
  该模型只服务搜索结果展现，MUST NOT 反过来成为 runtime snapshot 的 canonical storage model。
- **FR-010**: 现有 metadata resource URI 与 metadata tool name MUST 保持不变。
- **FR-011**: capability surface 与 `execute_query` payload MUST 保持不变。
- **FR-012**: index unsupported 语义 MUST 保持不变；不支持 index 的数据库仍返回 `unsupported`。
- **FR-013**: schema 为空或数据库无 schema 概念时，
  系统 MUST 反映真实数据库语义，MUST NOT 为了统一抽象而制造虚假的 schema / parent 结构。
- **FR-014**: 系统 SHOULD 允许 typed metadata nodes 保留数据库特有的扩展字段，
  而不是要求所有差异都回填进一套 universal flat field set。
- **FR-015**: 本轮 MUST 更新当前假设 `items -> MetadataObject` 的测试、契约和集成验证。
- **FR-016**: `MetadataObject` SHOULD 被移除，
  或降级为不进入 metadata source-of-truth / query / response 主路径的兼容适配层。
- **FR-017**: 本轮实现 MUST 只修改 metadata 相关生产代码、测试代码和本规格文档，
  不得夹带无关命名调整、循环改写、格式性重构或其他小修小改。
- **FR-018**: 本特性 MUST 提供 dedicated tests，
  覆盖 loader hierarchy、query navigation、resource payload、tool payload、
  search summary 与 unsupported index 回归。

### Key Entities *(include if feature involves data)*

- **RuntimeMetadataCatalog**: runtime 持有的 metadata 总入口，组织多 logical databases 的 native metadata trees。
- **DatabaseMetadata**: 一个 logical database 的顶层元数据节点，包含 database 基本信息与其 schema 集合。
- **SchemaMetadata**: schema 节点，拥有 tables 与 views。
- **TableMetadata**: table 节点，拥有 columns 与 indexes。
- **ViewMetadata**: view 节点，拥有 columns。
- **ColumnMetadata**: column 节点，表达 column 自身属性，不通过 parent 字段补关系。
- **IndexMetadata**: index 节点，表达 index 自身属性及其归属 table。
- **MetadataSearchHit**: 搜索专用摘要对象，用于跨 object type 聚合结果，不作为 canonical storage model。

### Assumptions

- 本轮允许 metadata payload shape 调整；这是一个显式契约变化，不被视为纯内部重构。
- 为了减少 transport churn，list / search 结果可以继续沿用 `items` 和 `next_page_token` envelope。
- 本轮优先解决“结构与对象边界”问题，不要求一次性暴露所有 JDBC vendor-specific metadata 列。
- capability、session、SQL execute 和非 metadata transport 入口不在本轮范围内。

## Non-Goals

- 不新增 public resource URI，也不新增或重命名 metadata tool。
- 不改 capability 模型、session 模型、`execute_query` 成功 payload 或错误模型。
- 不把本轮扩大成 transport 层重写、URI dispatch 重做或 session serialization follow-up。
- 不要求一次性支持所有 vendor-specific metadata 字段的完全透传。
- 不允许借这轮需求顺手清理无关代码。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: metadata read/query/response 主路径中的 `MetadataObject` 生产代码使用次数降为 `0`，
  或只保留在明确标注的兼容适配层中。
- **SC-002**: runtime snapshot 的 source of truth 变为 database -> schema -> table/view -> column/index
  的 native hierarchy。
- **SC-003**: metadata resource 与 metadata tool 返回的 item/detail payload
  100% 不再以 `MetadataObject` 作为对外结果模型。
- **SC-004**: `describe_table` 与 `describe_view` 不再通过拼装多条平铺对象构造 detail 结果。
- **SC-005**: index unsupported 语义、resource URI 与 metadata tool name 保持零漂移。

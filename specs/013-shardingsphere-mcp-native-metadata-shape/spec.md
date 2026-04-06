# Feature Specification: ShardingSphere MCP Direct Typed Metadata Model Without MetadataObject

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-05  
**Status**: Design draft  
**Input**: User description:
"MetadataObject过度设计了。我就希望数据库里是什么样子，展现就是什么样子。比如说库schema表列索引，你为什么要过度设计，过度抽象呢？你就把原来的样子展现出来吧。用speckit设计我的需求，但不允许切换分支"

## Scope Statement

本 follow-up 继续只处理 `mcp/core` metadata 主链路与紧邻测试中的模型和展现方式：

- `MetadataObject` 从 metadata loader、snapshot、query、resource、tool 主链路中彻底移除
- runtime metadata 直接使用具体对象：
  `MCPDatabaseMetadata -> MCPSchemaMetadata -> MCPTableMetadata / MCPViewMetadata -> MCPColumnMetadata / MCPIndexMetadata`
- `DatabaseMetadataSnapshot` 直接持有完整 `MCPDatabaseMetadata`，不再拆成平铺字段或镜像字段
- `MCPJdbcMetadataLoader` 直接构建 typed metadata tree，不再先 flatten 再 rebuild
- metadata resource 与 metadata tool 直接返回具体对象，而不是 generic item
- 保持现有 resource URI、metadata tool name、capability surface 与 `execute_query` surface 不变
- 本轮只允许修改 metadata 相关生产代码、测试代码和 Speckit 文档，不允许夹带无关小修小改

本特性的核心不只是让 payload 更漂亮，
而是把 “具体对象就是源模型” 作为 metadata 设计原则固定下来。

## Problem Statement

当前问题已经从 “为什么返回 `MetadataObject`” 进一步收敛为：

为什么 runtime source of truth 还没有直接使用具体 metadata 对象。

已知现状包括：

- JDBC `DatabaseMetaData` 天然提供的是 database / schema / table / view / column / index 的层级结构
- 这些结构先被压成 `MetadataObject`
- 然后再由额外步骤重新拼回 `MCPSchemaMetadata`、`MCPTableMetadata`、`MCPViewMetadata` 等 typed objects
- `DatabaseMetadataSnapshot` 还同时保留了平铺字段与层级字段
- `DatabaseMetadataSnapshots` 还维护了全局 `metadataObjects` 聚合列表

这会带来五个直接问题：

1. 具体对象已经存在，但 source of truth 仍然不是具体对象  
2. loader 做了一次 flatten，snapshot/query 又做了一次 rebuild，路径绕远了  
3. `MetadataObject` 即使不再是主返回模型，仍然拖着兼容缓存、聚合列表和测试夹具一起存在  
4. 以后要扩展 table/view/column/index 的真实字段时，还会被 flat 中间层阻挡  
5. 设计上仍然没有兑现 “数据库里什么样，metadata 就是什么样”

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Runtime snapshot 直接持有完整 MCPDatabaseMetadata (Priority: P1)

作为 MCP 维护者，我希望 runtime snapshot 的事实源就是完整 `MCPDatabaseMetadata`，
这样 metadata 主链路里不再存在 flat 中间模型。

**Why this priority**: 如果 snapshot 还保留 `MetadataObject`，
那 resource/tool 的直接返回只是表面改善，内部设计仍然绕了一圈。

**Independent Test**: `MCPJdbcMetadataLoaderTest` 与 `DatabaseMetadataSnapshotsTest`
可以独立验证 snapshot 直接持有 `MCPDatabaseMetadata`，
且不存在 `MetadataObject` 聚合依赖。

**Acceptance Scenarios**:

1. **Given** JDBC loader 读取到一个 logical database 的 metadata，
   **When** 构建 snapshot，**Then** snapshot 必须直接持有完整 `MCPDatabaseMetadata`，
   而不是保存 `databaseType + version + List<MetadataObject> + rebuilt schemas`。
2. **Given** runtime catalog 中存在多个 logical databases，
   **When** 聚合 snapshots，**Then** 系统必须围绕 `MCPDatabaseMetadata` 导航，
   而不是维护全局 `metadataObjects` 列表。
3. **Given** refresh 某个 database，
   **When** snapshot 被替换，**Then** 替换的对象必须是完整 typed metadata tree，
   而不是一批平面记录。

---

### User Story 2 - Loader 直接产出具体 metadata 对象 (Priority: P1)

作为 MCP 维护者，我希望 loader 直接构建 `MCPDatabaseMetadata`、`MCPSchemaMetadata`、
`MCPTableMetadata`、`MCPViewMetadata`、`MCPColumnMetadata`、`MCPIndexMetadata`，
这样系统不再需要 `MetadataObject` 和 rebuild helper。

**Why this priority**: 真正删掉 `MetadataObject` 的关键不在 response，而在 loader。

**Independent Test**: `MCPJdbcMetadataLoaderTest` 可以独立验证 schema/table/view/column/index
的 ownership 层级与去重语义。

**Acceptance Scenarios**:

1. **Given** JDBC metadata 中包含 table 与其 columns/indexes，
   **When** loader 执行，**Then** 必须直接把 columns/indexes 放进所属 `MCPTableMetadata`，
   不能先形成平面 `MetadataObject` 再重建。
2. **Given** JDBC metadata 中包含 view 与其 columns，
   **When** loader 执行，**Then** 必须直接把 columns 放进所属 `MCPViewMetadata`。
3. **Given** schema 为空、默认 schema 或数据库无 schema 概念，
   **When** loader 执行，**Then** 必须保留真实语义，不能为了适配 flat 模型补造关系字段。

---

### User Story 3 - Resource 与 tool 直接返回具体对象 (Priority: P1)

作为 MCP metadata 使用者，我希望 list/detail/search 结果直接对应具体对象，
这样看到的模型和 runtime source of truth 一致。

**Why this priority**: 只有当 source model 和 output model 都是具体对象，
“数据库里什么样，展现就是什么样” 才算真正落地。

**Independent Test**: `ResourceHandlerTest`、`MCPResourceControllerTest`、
`MetadataToolDispatcherTest` 和 `MCPToolPayloadResolverTest`
可独立验证 typed payload。

**Acceptance Scenarios**:

1. **Given** 调用 `describe_table(database, schema, table)`，
   **When** tool dispatch 完成，**Then** 返回必须是一个 `MCPTableMetadata` detail object，
   并直接包含 columns 与 indexes。
2. **Given** 读取 table/view/column/index 相关 resource，
   **When** 返回 payload，**Then** `items` 中必须是对应的具体 metadata objects，
   而不是 `MetadataObject`。
3. **Given** 调用 `search_metadata(...)`，
   **When** 聚合多种 object type，**Then** 允许返回 `MetadataSearchHit` 这类搜索投影，
   但该投影不能回退成 runtime source model。

### Edge Cases

- schema 为空、默认 schema 或数据库无 schema 概念时，typed model 必须保留真实语义。
- index 继续是 capability-gated optional object；不支持时仍返回 `unsupported`。
- loader 仍需维持当前 schema 去重、index 去重与 object ownership 语义。
- list / search 的分页 envelope 可以保留，但 envelope 中的 item 不能再依赖 `MetadataObject`。
- `MetadataObjectType` 可以保留给 capability / search / tool object-type 参数使用，但它不再绑定 `MetadataObject` 这个类。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `MetadataObject` MUST 从 `mcp/core` metadata 主链路中彻底移除，
  包括 loader、snapshot、query、resource、tool 和对应测试夹具。
- **FR-002**: `DatabaseMetadataSnapshot` MUST 直接持有一个完整 `MCPDatabaseMetadata` 实例，
  MUST NOT 再分别持有 `databaseType`、`databaseVersion`、`List<MetadataObject>` 和 rebuilt schemas。
- **FR-003**: `DatabaseMetadataSnapshots` MUST 以 logical database -> typed snapshot 的方式组织 runtime catalog，
  MUST NOT 再维护全局 `List<MetadataObject>` 聚合缓存。
- **FR-004**: `MCPJdbcMetadataLoader` MUST 直接构建 `MCPDatabaseMetadata`、
  `MCPSchemaMetadata`、`MCPTableMetadata`、`MCPViewMetadata`、`MCPColumnMetadata`、`MCPIndexMetadata`，
  MUST NOT 先构建 flat intermediate records。
- **FR-005**: `MetadataHierarchyBuilder` SHOULD 被删除；
  如果临时保留，MUST 只服务 typed metadata 构建，MUST NOT 再依赖 `MetadataObject`。
- **FR-006**: `MetadataQueryService` MUST 以具体 metadata 对象为 source of truth，
  通过 typed hierarchy 导航完成 list / detail 查询。
- **FR-007**: metadata resource 的返回值 MUST 改为贴近所请求对象类型的 typed payload；
  list resource 可以保留 `items` envelope，但 `items` 内容 MUST 不再是 `MetadataObject`。
- **FR-008**: metadata tool 的 list 系列结果 MUST 返回 typed metadata objects；
  `describe_table` MUST 返回 `MCPTableMetadata`，
  `describe_view` MUST 返回 `MCPViewMetadata`。
- **FR-009**: `describe_table` 的 detail payload MUST 直接包含 columns 与 indexes；
  `describe_view` 的 detail payload MUST 直接包含 columns。
- **FR-010**: `search_metadata` MUST 使用 dedicated search summary model 或等价结果投影，
  且该投影 MUST NOT 成为 runtime canonical storage model。
- **FR-011**: 现有 metadata resource URI 与 metadata tool name MUST 保持不变。
- **FR-012**: capability surface 与 `execute_query` payload MUST 保持不变。
- **FR-013**: index unsupported 语义 MUST 保持不变；不支持 index 的数据库仍返回 `unsupported`。
- **FR-014**: schema 为空或数据库无 schema 概念时，
  系统 MUST 反映真实数据库语义，MUST NOT 为了统一抽象制造虚假的 parent 结构。
- **FR-015**: `MetadataObjectType` MAY 保留为 object category enum，
  供 capability、tool 参数与 search filter 使用，但 MUST 与 `MetadataObject` 解耦。
- **FR-016**: typed metadata 节点 SHOULD 允许后续保留数据库特有扩展字段，
  而不是要求所有差异回填进一套 universal flat fields。
- **FR-017**: 本轮 MUST 更新所有假设 `items -> MetadataObject`、`snapshot -> metadataObjects`
  的测试、契约与集成验证。
- **FR-018**: 本轮实现 MUST 只修改 metadata 相关生产代码、测试代码和本规格文档，
  不得夹带无关命名调整、循环改写、格式性重构或其他小修小改。
- **FR-019**: 本特性 MUST 提供 dedicated tests，
  覆盖 direct loader build、typed snapshot storage、query navigation、resource payload、
  tool payload、search summary 与 unsupported index 回归。

### Key Entities *(include if feature involves data)*

- **MCPDatabaseMetadata**: logical database 的完整 metadata 根节点，包含 database 名称、type、version 与 schemas。
- **MCPSchemaMetadata**: schema 节点，拥有 tables 与 views。
- **MCPTableMetadata**: table 节点，拥有 columns 与 indexes。
- **MCPViewMetadata**: view 节点，拥有 columns。
- **MCPColumnMetadata**: column 节点，表达 column 自身及其所属 table/view。
- **MCPIndexMetadata**: index 节点，表达 index 自身及其所属 table。
- **DatabaseMetadataSnapshot**: refresh / catalog 管理用容器，直接持有一个完整 `MCPDatabaseMetadata`。
- **MetadataSearchHit**: 搜索专用摘要对象，用于跨 object type 聚合结果，不作为 canonical storage model。
- **MetadataObjectType**: object category enum，用于 capability、search 与 tool 参数，不再绑定 flat object。

### Assumptions

- 本轮允许 metadata payload shape 调整；这是一个显式契约变化，不被视为纯内部重构。
- 为了减少 transport churn，list / search 结果可以继续沿用 `items` 和 `next_page_token` envelope。
- 本轮优先解决 “源模型就是具体对象” 的问题，不要求一次性暴露所有 JDBC vendor-specific metadata 列。
- capability、session、SQL execute 和非 metadata transport 入口不在本轮范围内。

## Non-Goals

- 不新增 public resource URI，也不新增或重命名 metadata tool。
- 不改 capability 模型、session 模型、`execute_query` 成功 payload 或错误模型。
- 不把本轮扩大成 transport 层重写、URI dispatch 重做或 session serialization follow-up。
- 不要求一次性支持所有 vendor-specific metadata 字段的完全透传。
- 不允许借这轮需求顺手清理无关代码。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `rg -n "\\bMetadataObject\\b" mcp/core mcp/bootstrap` 的生产代码匹配数降为 `0`。
- **SC-002**: runtime snapshot 的 source of truth 变为 `MCPDatabaseMetadata` typed tree，
  不再存在 `metadataObjects` 聚合缓存。
- **SC-003**: metadata resource 与 metadata tool 返回的 item/detail payload
  100% 不再以 `MetadataObject` 作为对外结果模型。
- **SC-004**: `describe_table` 与 `describe_view` 不再通过拼装多条平铺对象构造 detail 结果。
- **SC-005**: loader 不再执行 `MetadataObject -> hierarchy` 的二次重建流程。
- **SC-006**: index unsupported 语义、resource URI 与 metadata tool name 保持零漂移。

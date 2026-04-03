# Feature Specification: ShardingSphere MCP Generic URI Template Dispatch and Executing Resource Handlers

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-03  
**Status**: Design draft  
**Input**: User description:
"我对这个MVC的设计仍然不够满意，继续给我精进。
ResourceHandler只是用于解析uri里边的占位符，并且生成查询计划，
而不是真正的去执行了查询。我希望用一个更通用的方式去解析URI里面的占位符，
然后handler是去真正的去处理查询" and "开始做吧，用speckit设计。不要切换分支"

## Scope Statement

本 follow-up 不改变 `001-shardingsphere-mcp` 已固定的 MCP public resource surface，
只收敛 `mcp/core` 内部的 URI 模板解析和 resource dispatch 执行边界：

- 抽出 repository-owned 的通用 URI template 基础设施，供 resource 使用，并为未来 tool URI 场景预留复用点
- 让 `ResourceHandler` 从“解析 URI 后生成查询计划”的伪 handler，收敛为“命中后真正执行查询/能力获取”的执行型 handler
- 让 `MCPResourceController` 只保留 resource URI 总入口、unsupported URI 错误处理和 payload 映射职责
- 删除当前 `ResourceQueryPlan` 这类中间跳板模型，把执行分支下沉到具体 handler
- 保持 `mcp/core` 继续不依赖第三方 Web / JAX-RS framework，不引入 Jersey

本特性的核心不是新增 public resources，也不是把 tool 整体改造成 URI dispatch，
而是让 “URI 模板如何匹配” 与 “命中的 resource 如何执行” 落到正确层级。

## Problem Statement

当前 resource dispatch 已从集中式大 resolver 拆成 controller + dispatcher + handler，
但 handler 仍未真正承担执行职责。

已知问题包括：

- `ResourceHandler` 当前只负责把 `resourceUri` 占位符翻译成 `ResourceQueryPlan`，
  名称与职责不一致
- `MCPResourceController` 仍根据 `ResourceQueryPlan` 二次分支执行 capability 与 metadata 读取，
  controller 过重
- URI 模板匹配能力仍放在 resource 专用类中，难以复用于未来可能出现的 tool URI surface
- `ResourceQueryPlan` 只是中间跳板，增加了一层额外解释成本，但没有形成清晰的稳定抽象

## User Scenarios & Testing *(mandatory)*

### User Story 1 - URI 模板匹配成为 core 可复用的通用基础设施 (Priority: P1)

作为 MCP 维护者，我希望 URI template 的匹配、变量提取和冲突检测不再绑定在 resource 子域，
这样后续如果 tool 或其他 MCP surface 也需要 URI template，就可以复用同一套语义。

**Why this priority**: 当前 `ResourceUriMatcher` / `ResourceUriMatch` 已经承担了通用模板匹配能力，
但名字和包边界都过于 resource-specific，阻碍复用和抽象层次的一致性。

**Independent Test**: 通用 URI template 单元测试可独立验证 strict match、变量提取、
invalid template、invalid uri 和 template overlap 行为，而无需依赖 resource 业务。

**Acceptance Scenarios**:

1. **Given** 一个模板 `shardingsphere://databases/{database}/schemas/{schema}`，**When** 匹配
   `shardingsphere://databases/logic_db/schemas/public`，**Then** 系统必须返回匹配成功，
   并提取 `database=logic_db`、`schema=public`。
2. **Given** 两个模板 `shardingsphere://databases/{database}` 和
   `shardingsphere://databases/default_db`，**When** registry 初始化，**Then** 系统必须识别
   二者存在重叠匹配风险并 fail-fast，而不是把歧义留到运行时。
3. **Given** 后续新增一个非 resource 的 URI surface，**When** 其路由层复用 URI template
   基础设施，**Then** 无需复制第二套 segment 解析和变量提取逻辑。

---

### User Story 2 - Resource handler 在命中后直接执行查询或能力读取 (Priority: P1)

作为 MCP 维护者，我希望 `ResourceHandler` 真正处理已命中的 resource URI，
直接执行 capability 查询或 metadata 读取，而不是只返回一份中间计划给 controller 再解释执行。

**Why this priority**: 当前 `ResourceHandler` 的职责更像 “URI translator”，
这让 controller 和 handler 之间的抽象层级倒挂，也让 `ResourceQueryPlan` 成为无必要的中间层。

**Independent Test**: resource handler dedicated tests 可直接验证每个 handler 在给定
`UriTemplateMatch` 和 context 时返回正确的 domain result，而 controller 不再需要知道内部执行分支。

**Acceptance Scenarios**:

1. **Given** 请求 URI `shardingsphere://capabilities`，**When** 命中 service capability handler，
   **Then** handler 直接返回 service capability 领域结果，而不是返回中间计划。
2. **Given** 请求 URI `shardingsphere://databases/logic_db/capabilities`，**When** 命中
   database capability handler 且 capability 存在，**Then** handler 直接返回该 capability 结果。
3. **Given** 请求 URI 为 table column metadata 资源，**When** 命中对应 handler，
   **Then** handler 必须直接调用 metadata reader 完成查询，而不是让 controller 再解释查询计划。

---

### User Story 3 - Controller 只保留入口和 payload 映射，public contract 零损失 (Priority: P1)

作为 MCP 客户端和运行方，我希望这轮重构只改变 internal dispatch 执行边界，
而不改变 `resources/list`、`resources/read`、错误码语义和 payload 形状。

**Why this priority**: 本次价值来自架构层次纠正；只要 public resource contract 发生漂移，
就不再是一次纯粹的内部收敛。

**Independent Test**: 现有 controller、capability builder、bootstrap resource specification
回归继续通过，并新增通用 URI template 与 executing handler 测试。

**Acceptance Scenarios**:

1. **Given** 当前 V1 已固定的 16 条 resource URI，**When** 客户端读取 `resources/list`，
   **Then** 暴露集合、顺序与 public contract 保持不变。
2. **Given** 某个不支持的 resource URI，**When** client 发起 `resources/read`，
   **Then** controller 仍返回当前 `invalid_request` 错误 payload。
3. **Given** index resource 在当前数据库类型不支持，**When** handler 执行 metadata 读取，
   **Then** 最终返回的 error payload 与当前行为一致。

### Edge Cases

- 不允许 `UriTemplate` 基础设施直接依赖 resource 或 tool 语义。
- 不允许 `ResourceHandler` 直接返回 `Map<String, Object>`，避免 transport payload 泄漏到执行层。
- 不允许 `ResourceDispatcher` 继续返回 `ResourceQueryPlan` 或等价中间计划类型。
- 不允许把 tool 整体改成 URI dispatch 作为本轮前置条件。
- 不允许引入 Jersey、Undertow 或其他第三方 URI template SDK。
- 不允许改变 `001-shardingsphere-mcp/contracts/mcp-domain-contract.md` 已定义的 public resource set。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在 `mcp/core` 中提供 repository-owned 的通用 URI template 抽象，
  用于表达模板、执行匹配、提取路径变量和检测模板重叠。
- **FR-002**: 通用 URI template 抽象 MUST 保持 transport-neutral，
  不依赖 resource-specific、tool-specific 或 MCP SDK 类型。
- **FR-003**: 通用 URI template 匹配 MUST 继续采用 strict full match 语义：
  scheme 一致、segment 数一致、字面量 segment 全等、变量 segment 提取变量值、空 segment 非法。
- **FR-004**: 系统 MUST 在 registry 初始化阶段检测重复 template 与 overlap template，
  并在发现冲突时 fail-fast。
- **FR-005**: `ResourceDispatcher` MUST 只负责路由到唯一命中的 handler，
  输出 `ResourceHandlerExecution` 或等价执行描述对象，而不是返回查询计划。
- **FR-006**: `ResourceHandler` MUST 接收已匹配的 URI variables 与执行上下文，
  并直接执行业务查询或 capability 获取。
- **FR-007**: 系统 MUST 提供 `ResourceHandlerContext` 或等价对象，
  统一向 handler 暴露 runtime context、capability access 和 metadata reader 能力。
- **FR-008**: 系统 MUST 提供 `ResourceHandlerResult` 或等价统一结果模型，
  供 executing handler 返回 service capability、database capability、metadata result 或 domain error。
- **FR-009**: `MCPResourceController` MUST 成为 resource URI 的总入口，
  负责 unsupported URI 错误处理和 `ResourceHandlerResult` 到 payload 的映射，
  不再解释 `ResourceQueryPlan`。
- **FR-010**: 当前 `ResourceQueryPlan` MUST 被移除，或降为不存在于 resource read 主路径中的实现细节。
- **FR-011**: 当前 resource-specific 的 URI matcher / match 类型 SHOULD 被
  通用 URI template 类型替代，避免与未来 tool URI 场景重复实现。
- **FR-012**: 本轮 MUST 保持 `MCPResourceController` 的返回值继续为 `Map<String, Object>`，
  不改变 bootstrap 对 controller 的使用方式。
- **FR-013**: 本轮 MUST 保持 `resources/list` 和 `resources/read` 的 public contract、
  URI surface、错误语义和 payload 形状不变。
- **FR-014**: 本轮 MUST NOT 引入 Jersey 或其他第三方 URI template / JAX-RS 依赖到 `mcp/core`。
- **FR-015**: 本特性 MUST 提供通用 URI template 测试、resource handler executing 测试、
  controller 回归测试和 bootstrap 兼容验证路径。

### Key Entities *(include if feature involves data)*

- **UriTemplate**: core 中的通用 URI template 对象，负责模板编译、strict match 和 overlap 判断。
- **UriTemplateMatch**: 一次模板匹配结果，包含模板文本和有序变量映射。
- **ResourceHandlerContext**: resource handler 的执行上下文，暴露 capability builder、metadata reader
  和 runtime metadata 等执行依赖。
- **ResourceHandlerResult**: resource handler 执行后的统一领域结果，覆盖 service capability、
  database capability、metadata result 和 domain error。
- **ResourceHandlerExecution**: dispatcher 路由后的执行描述，包含命中的 handler 与对应 `UriTemplateMatch`。

### Assumptions

- 当前 public resource URI 继续沿用 `specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`
  已定义的 16 条 surface。
- `MetadataResourceReader` 继续作为 metadata 读取执行器保留，不被拆散到 16 个 handler 内部实现细节之外。
- `MCPResourceController` 继续作为 `mcp/core` 中 resource URI 的总入口，对 bootstrap 提供稳定入口。
- future tool URI surface 不是本轮必做项，但通用 URI template 基础设施需要为其复用留出空间。

## Non-Goals

- 不新增 public resources，不修改 URI 命名，不调整既有 resource 顺序。
- 不把 `MCPToolPayloadResolver`、`MetadataToolDispatcher` 或 tool public contract 一并改造成 URI dispatch。
- 不在本轮引入新的 SPI 体系、注解式路由系统或 Web framework。
- 不让 executing handler 直接生成 transport payload map。
- 不把本轮设计扩展成完整 RFC 6570 URI engine；只覆盖当前 MCP resource/template 需要的 strict subset。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 的 resource URI 模板匹配逻辑由通用 URI template 层承接，
  `mcp/resource/dispatch` 中不再保留 resource-specific 的模板解析实现。
- **SC-002**: 100% 的 resource handlers 都直接返回 executing result，
  `ResourceQueryPlan` 从 resource read 主链路中消失。
- **SC-003**: `MCPResourceController` 中按 plan type 分支执行的逻辑降为 `0`，
  仅保留 unsupported URI 处理与 payload mapping。
- **SC-004**: registry 在启动期即可拦住 duplicate template 与 overlap template，
  运行期 ambiguity 分支不再依赖作为主要防线。
- **SC-005**: `resources/list`、`resources/read`、resource payload 和 error surface
  在重构后保持零漂移。

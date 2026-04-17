# Implementation Plan: ShardingSphere-Proxy Encrypt and Mask MCP V1

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-17 | **Spec**: `/.specify/specs/001-proxy-encrypt-mask-mcp/spec.md`
**Input**: Feature specification from `/.specify/specs/001-proxy-encrypt-mask-mcp/spec.md`

## Summary

本特性要在现有 MCP 基础上，为 ShardingSphere-Proxy 提供一套面向自然语言的加密与脱敏规则工作流。
它不是让大模型直接自由拼 SQL，而是把“澄清需求、推荐算法、规划物理 DDL、生成 DistSQL、按审批模式执行、执行后校验”
收敛成一套可审阅、可分步、可验证的 MCP 能力。

实现上优先复用三类现有能力：

- `mcp/core` 里的工具、资源、元数据目录与 SQL 执行入口；
- `features/encrypt` / `features/mask` 里的 DistSQL 语义、规则校验和算法 SPI；
- `proxy/backend/core` 里的 DistSQL 执行链路。

V1 的范围刻意收窄为“单数据库、单表、单列”的交互式工作流，支持加密和脱敏两条主线。
其中 encrypt 覆盖新建与修改，mask 覆盖新建、修改、删除。
其中加密可能伴随物理列与索引 DDL，脱敏通常是规则侧变更；两者都必须经过计划、审阅、执行、验证四段式流程。

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**:
- `mcp/core` 的 Tool/Resource/Metadata 基础设施
- ShardingSphere-Proxy DistSQL 执行链
- `features/encrypt` / `features/mask` DistSQL handler
- `TypedSPILoader` / `ShardingSphereServiceLoader`
**Storage**:
- 不新增持久化存储
- 运行态依赖 Proxy 元数据、MCP metadata catalog 与当前会话上下文
- 一步一步模式下的已确认计划通过服务端会话上下文保留
**Testing**:
- JUnit 5
- Mockito
- `./mvnw -pl mcp/core -am test`
- 必要时补充 Proxy 侧集成验证
**Target Platform**:
- ShardingSphere-Proxy
- MCP bootstrap 的 Streamable HTTP / STDIO 运行形态
**Project Type**: 现有仓库内的 Java 后端能力扩展
**Performance Goals**:
- 面向人工操作的交互式低延迟流程
- 避免全库扫描、避免样本数据读取作为默认路径
- 单次规划应尽量收敛为少量元数据读取与少量 DistSQL 检查
**Constraints**:
- 仅支持 `ShardingSphere-Proxy`
- MCP 运行时必须连接 Proxy，而不是直接连接底层物理库
- 必须显式提供 `database`，不能依赖 `USE`
- 必须先出全局步骤列表
- 必须支持“一起做”与“一步一步做”
- 物理 DDL 执行模式必须显式选择
- 不处理历史数据迁移 / 回填
- 不要求回滚
- 不要求审计落库
- 默认派生列类型遵循 ShardingSphere 当前默认策略，不做 MCP 自定义推断
**Scale/Scope**:
- V1 优先覆盖单数据库、单表、单列
- 支持 encrypt 的 create / alter
- 支持 mask 的 create / alter / drop
- 支持派生列索引建议或索引 DDL

## Constitution Check

*GATE: 已通过；Phase 1 设计完成后需再次复核。*

- **Proxy-First Logical Abstraction**
  - 通过。工作流始终以逻辑库、逻辑表、逻辑列为主视角。
  - 物理列、索引、DDL 只作为审阅与执行工件暴露。
- **Explicit Operator Control**
  - 通过。任何 DDL 或规则变更前，都必须先给步骤清单与执行模式。
  - 执行模式保留为 `auto-execute`、`review-then-execute`、`manual-only`。
- **Minimal Safe Automation**
  - 通过。V1 只做规则、元数据、SQL 可执行性验证，不做数据迁移、回填、回滚编排。
  - 样本数据只作为得到用户授权后的升级路径。
- **Deterministic Naming and Transparent Changes**
  - 通过。默认命名为 `*_cipher`、`*_assisted_query`、`*_like_query`。
  - 冲突时追加数字后缀，并把最终名称回传给用户。
  - 不默认复用历史同名物理列。
- **Complete Verification Before Completion**
  - 通过。完成标准包含 DDL、规则、逻辑元数据、逻辑 SQL 可执行性四层验证。

## Project Structure

### Documentation (this feature)

```text
specs/001-proxy-encrypt-mask-mcp/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- mcp-tools.md
`-- tasks.md
```

### Source Code (repository root)

```text
mcp/
|-- core/src/main/java/org/apache/shardingsphere/mcp/tool/
|-- core/src/main/java/org/apache/shardingsphere/mcp/resource/
|-- core/src/main/java/org/apache/shardingsphere/mcp/metadata/
`-- core/src/main/resources/META-INF/services/

features/
|-- encrypt/distsql/handler/src/main/java/org/apache/shardingsphere/encrypt/distsql/handler/
|-- encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/
|-- mask/distsql/handler/src/main/java/org/apache/shardingsphere/mask/distsql/handler/
`-- mask/core/src/main/java/org/apache/shardingsphere/mask/

infra/
`-- distsql-handler/src/main/java/org/apache/shardingsphere/distsql/handler/executor/ral/plugin/

proxy/
`-- backend/core/src/main/java/org/apache/shardingsphere/proxy/backend/handler/distsql/
```

**Structure Decision**:
主要实现落点放在 `mcp/core`，因为它负责 MCP Tool、Resource、Metadata Catalog 和执行编排。
Encrypt / Mask 模块与 Proxy 模块优先复用现成 DistSQL 与 SPI 能力，除非发现现有查询面不足，否则不在规则核心模块重复造轮子。

## Delivery Phases

### Phase 0 - Inspection Surface

建立规则与算法的 MCP 读取面，补齐以下可见性：

- 当前逻辑库下的 encrypt rules；
- 当前逻辑库下的 mask rules；
- Proxy 当前可见的 encrypt algorithm plugins；
- Proxy 当前可见的 mask algorithm plugins；
- 逻辑表 / 列 / 索引元数据与现有 MCP 资源的串联读取。

这一步的目标不是执行，而是让工作流先拥有“看见当前状态”的能力。

### Phase 1 - Planning and Recommendation

在 inspection 之上构建规划层，负责：

- 接收自然语言与必要结构化参数；
- 做缺失信息识别与追问；
- 给出全局步骤列表；
- 推荐 encrypt / mask 算法；
- 生成派生列命名方案；
- 生成可审阅的 DDL / DistSQL / 索引建议；
- 给出执行模式与验证计划。

这一步输出的是“已澄清、可审阅、尚未执行”的交付物。

### Phase 2 - Execution Orchestration

按用户选定模式执行：

- 自动执行；
- 审阅后执行；
- 仅生成工件，不执行。

同时统一进度反馈语义，让用户在每一步都知道：

- 已完成什么；
- 当前卡在哪个审批点；
- 下一步是什么；
- 哪些 SQL / DistSQL 已执行，哪些尚未执行。

### Phase 3 - Validation and Summary

把“完成”定义为四层验证全部结束：

- 物理 DDL 状态；
- 规则状态；
- 逻辑元数据状态；
- 逻辑 SQL 可执行性状态。

如果用户选 `manual-only`，则该阶段至少要输出待验证工件与建议验证 SQL，而不是直接宣称成功。

## Open Design Notes

- 加密与脱敏共用一个工作流入口，但在规划阶段分叉：
  - `encrypt` 走“规则 + 可选物理列 / 索引 DDL”；
  - `mask` 走“规则优先，通常无物理 DDL”。
- 由于 `execute_query` 明确禁止 `USE`，工作流必须把 `database` 做成一等输入，而不是隐式会话状态。
- 由于当前 `mcp/core` 的 SQL 执行与元数据装载仍是 JDBC 直连形态，后续实现必须显式收敛到“连接 Proxy、按 Proxy 逻辑视角校验”的运行拓扑。
- 由于当前加密 DDL 默认数据类型是 `VARCHAR(4000)`，V1 不引入新的列类型推断器。
- 由于当前仓库加密内部派生列后缀存在 `_C / _A / _L` 的实现约定，MCP 的 `*_cipher` 等命名策略必须显式产出，
  不能假设内核会自动生成相同名字。
- 由于 encrypt drop 已被推迟，V1 的删除生命周期只覆盖 mask。

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |

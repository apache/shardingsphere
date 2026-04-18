# Research: MCP Feature SPI Modularization

## Decision 1: 以 `ToolHandler` / `ResourceHandler` 作为 feature surface 的唯一 SPI 注册入口

- **Decision**: `mcp/features/encrypt` 和 `mcp/features/mask` 分别通过 `ToolHandler` 与 `ResourceHandler` SPI 直接注册自己的 tool 与 resource surface，`mcp/core` 的 surface 聚合只消费这两类 handler SPI。
- **Rationale**:
  - 这与用户“feature 模块与上层只通过 SPI 注册交互”的要求最对等。
  - 这样可以消除“feature 模块外部可插拔，但 feature 内部 tools/resources 仍靠 provider 手工枚举”的半插件化设计。
  - `ToolHandler` 和 `ResourceHandler` 本身已经是 SPI 接口，直接复用现有扩展模型比额外再包一层 provider 更简单、更纯粹。
- **Alternatives considered**:
  - 保留 `MCPFeatureProvider` 作为 surface assembly 唯一入口：拒绝，因为这会让 feature 内部仍然停留在手工组装 handler 的模式。
  - 同时支持 handler SPI 和 provider surface 两套注册来源：拒绝，因为会让 registry 规则和冲突来源变得不透明。

## Decision 2: 把 extension-facing contracts 整体提升到 `mcp/features/spi`

- **Decision**: 当前由 feature 实现直接依赖的 contract 类型从 `mcp/core` 提升到 `mcp/features/spi`，至少包括 `ToolHandler`、`ResourceHandler`、descriptor model、response contract，以及供 feature handler 访问共享能力的 runtime facade。
- **Rationale**:
  - 如果这些 contract 继续留在 `mcp/core`，feature 模块在编译期仍然直接依赖 core，实现上仍然是不对等的。
  - `mcp/features/spi` 必须成为唯一稳定边界，feature 才能在不触碰 core internals 的情况下独立演进。
- **Alternatives considered**:
  - 只调整注册方式，contract 继续留在 core：拒绝，因为 feature 仍然通过 core 类型与实现细节耦合。
  - 让 feature 直接依赖 `MCPRuntimeContext`、`MCPResponse` 等 core 类型：拒绝，因为这违背了“只通过 SPI 契约交互”的目标。

## Decision 3: `mcp/core` 只保留共享平台职责，不再持有 encrypt / mask workflow 语义

- **Decision**: `mcp/core` 继续保留 registry、controller、protocol、session、metadata、execution、URI 匹配等共享基础设施，但移除 encrypt / mask 业务规划、验证、算法推荐、规则检查和 feature resource 实现。
- **Rationale**:
  - 当前真正的不对等点，不只在 surface 注册，还在 workflow planning / validation / resource 语义都堆在 core。
  - 只有把这些 feature 语义一起下沉，模块拆分才有实际价值。
- **Alternatives considered**:
  - 继续保留 `WorkflowPlanningService` 一类共享服务，只在内部按 `featureType` 分支：拒绝，因为 core 仍然知道 encrypt / mask 差异。
  - 只移动 tool / resource handler 类，不移动 service：拒绝，因为 feature 语义仍然驻留在 core。

## Decision 4: SPI 公开“少量但足够”的 workflow subcontracts，而不是把所有 helper 都变成 SPI

- **Decision**: `mcp/features/spi` 允许存在少量细粒度 workflow subcontracts，例如 planner、applier、validator、workflow snapshot / store facade；但不把 recommendation、template、naming 等所有 helper 都提升成公共 SPI。
- **Rationale**:
  - 用户已经确认 `spi` 可以包含 finer-grained workflow subcontracts。
  - 但 SPI 面不能膨胀成“所有实现细节都公共化”，否则会削弱后续演进空间。
  - planner / applier / validator / snapshot store 是稳定边界；算法推荐和命名规则更适合留在 feature 内部实现。
- **Alternatives considered**:
  - 只保留 handler contract，不提供任何 workflow seam：拒绝，因为 feature 仍然需要一套稳定的 workflow 组合边界。
  - 把所有 helper service 都提升到 `spi`：拒绝，因为会造成过度设计和不必要的长期兼容负担。

## Decision 5: tool family 按 feature 拆分，而不是继续共享 `encrypt_mask` 命名

- **Decision**:
  - encrypt 对外发布 `plan_encrypt_rule`、`apply_encrypt_rule`、`validate_encrypt_rule`
  - mask 对外发布 `plan_mask_rule`、`apply_mask_rule`、`validate_mask_rule`
- **Rationale**:
  - 产品尚未发布，不需要为了兼容保留共享工作流命名。
  - feature-specific tool family 更符合 ownership，也避免一个 tool family 承担多个 feature 语义。
- **Alternatives considered**:
  - 保留 `plan_encrypt_mask_rule` 一类共享命名：拒绝，因为这会把 encrypt 与 mask 的 surface 继续捆绑在一起。
  - 拆成更细碎的每一步独立 tool：拒绝，因为目前业务工作流仍以 plan / apply / validate 三段为稳定骨架。

## Decision 6: URI 采用 feature-scoped namespace，而不是沿用现有平铺路径

- **Decision**:
  - encrypt 资源 URI 收敛到 `shardingsphere://features/encrypt/...`
  - mask 资源 URI 收敛到 `shardingsphere://features/mask/...`
- **Rationale**:
  - URI 首发即应表达 feature ownership，避免继续把 feature surface 混在平铺命名下。
  - feature namespace 能天然降低重叠和歧义，也更利于未来加新 feature。
- **Alternatives considered**:
  - 保留 `shardingsphere://databases/{database}/encrypt-rules` 等旧路径：拒绝，因为 ownership 不明显，而且和 pluginized surface 不匹配。
  - 使用单独的 `plugin` 前缀：拒绝，因为最终模块命名已经确定为 `features`，URI 也应保持一致。

## Decision 7: reactor 结构保持 `features -> spi / encrypt / mask`，bootstrap 只负责把官方 feature jars 带入运行时

- **Decision**:
  - 保持 `mcp/features/pom.xml`
  - `mcp/core -> mcp/features/spi`
  - `mcp/features/encrypt -> mcp/features/spi`
  - `mcp/features/mask -> mcp/features/spi`
  - `mcp/bootstrap` 可以在打包层带入 encrypt / mask feature jar，但不直接引用实现类
- **Rationale**:
  - 这样可以在不改变已确认模块结构的前提下，落实“feature 只依赖 SPI、不依赖 core internals”的设计目标。
  - 当前目标是代码层和 surface 发现层插件化，不额外扩展到外部插件目录或运行时动态装载。
- **Alternatives considered**:
  - `encrypt / mask -> core`：拒绝，因为这会把 feature 变成 core 的附属目录。
  - 额外新增 runtime 聚合模块：暂不采用，因为当前模块结构已经固定，这一轮不扩展 reactor 形态。

## Decision 8: surface 聚合必须确定且显式失败，不能允许静默覆盖

- **Decision**: 对 duplicate tool name、duplicate / overlapping URI pattern、空 surface、无效 SPI 注册统一在 registry 装配阶段显式失败。
- **Rationale**:
  - 用户已明确要求对歧义注册做显式拒绝。
  - feature 插件化之后，启动期 fail-fast 比运行期 silent shadowing 更重要。
- **Alternatives considered**:
  - 先到先得：拒绝，因为 discovery surface 会随 classpath 顺序漂移。
  - 后者覆盖前者：拒绝，因为 reviewer 很难定位真实生效来源。

## Decision 9: `MCPFeatureProvider` 不再作为 tool/resource 装配链路的一部分

- **Decision**: 本次 feature modularization 的验收边界不再依赖 `MCPFeatureProvider` 参与 tool / resource 装配；若将来仍需 feature-level metadata SPI，应单独定义，并且不能作为 surface assembly 的前置条件。
- **Rationale**:
  - 从当前代码看，`MCPFeatureProvider` 的唯一实际价值主要是 featureType 去重，而不是运行时 surface 执行所必需。
  - 保留它参与 surface assembly 只会让 registry 模型更复杂，而不会带来新的用户价值。
- **Alternatives considered**:
  - 继续以 provider 承担 featureType 校验和 handler 枚举：拒绝，因为 handler 直接 SPI 已经足够表达 surface。
  - 直接为当前 feature 引入新的 metadata SPI：暂不采用，因为当前需求的核心是 surface pluginization，而不是 feature catalog 功能。

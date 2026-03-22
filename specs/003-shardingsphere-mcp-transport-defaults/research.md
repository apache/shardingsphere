# Research: ShardingSphere MCP Transport Default Realignment

## Decision 1: 发行包默认值改为 STDIO-first

- **Decision**: 默认发行包改为 `stdio.enabled = true`、`http.enabled = false`。
- **Rationale**:
  - 本地 Agent、IDE、CLI 集成对 `stdio` 的默认接入成本最低。
  - 当前数据库类 MCP 生态里，许多本地型 server 默认使用 `stdio`，而远程 HTTP 更常见于托管或明确的独立服务部署。
  - ShardingSphere 当前 MCP 发行包仍然是自带 runtime 的独立分发，不是厂商托管的远程 MCP 服务，因此默认值更应优先服务本地接入。
- **Alternatives considered**:
  - 保持 `http = true`、`stdio = false`: rejected，因为与当前主流本地 MCP host 的使用习惯不一致，且会在本地开发时额外暴露网络监听。
  - 默认双开 `http = true`、`stdio = true`: rejected，因为默认面过宽，且会让“本地默认模式”和“远程部署模式”继续混淆。

## Decision 2: 保留 HTTP 与 STDIO 双 transport 能力

- **Decision**: 不移除任一 transport，继续允许 `stdio only`、`http only` 与 `dual enabled` 三种合法运行模式。
- **Rationale**:
  - HTTP 仍然是远程 host、网关、反向代理与独立服务部署的必要入口。
  - STDIO 仍然是本地子进程、桌面 Agent、IDE 插件和冒烟验证的自然入口。
  - 改默认值不应被误解为缩减产品能力。
- **Alternatives considered**:
  - 只保留 STDIO: rejected，因为会丢失远程 host 接入与独立网络服务部署能力。
  - 只保留 HTTP: rejected，因为会提高本地 MCP host 的接入成本，并偏离 stdio-first 的本地运行习惯。

## Decision 3: 不改 wire protocol，只改默认启动与运维文档

- **Decision**: 本次 follow-up 不修改 `Streamable HTTP` contract、STDIO API surface、session 管理语义和 `/mcp` endpoint 规则。
- **Rationale**:
  - 变更目标是启动默认值与运维入口，而不是 transport 协议重构。
  - 当前 HTTP contract、session header、SSE 与 origin 校验已有实现与测试，重写协议会放大变更面而无必要收益。
- **Alternatives considered**:
  - 顺带调整 HTTP contract: rejected，因为超出问题边界，并会破坏已有 integration tests 的稳定性。

## Decision 4: 用启动矩阵测试收敛文档与实现

- **Decision**: 将 `stdio only`、`http only`、`dual enabled`、`both disabled` 作为显式测试矩阵。
- **Rationale**:
  - 当前仓库已经出现“默认配置”和“设计文档”不一致的问题。
  - 单点 smoke 无法保证默认值、合法组合与 fail-fast 约束长期同步。
- **Alternatives considered**:
  - 仅更新 README: rejected，因为无法防止代码与文档再次漂移。
  - 仅更新 YAML 默认值: rejected，因为审阅者仍无法从设计与测试中确认支持矩阵。

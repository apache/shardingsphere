# Feature Specification: ShardingSphere MCP Official Registry Publication and OCI Distribution

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-05  
**Status**: Design draft  
**Input**: User description:
"现在把这些都补全吧，使用speckit补充你的设计。但不允许切换分支"

## Scope Statement

本 follow-up 只补齐 ShardingSphere MCP 进入官方 MCP Registry 所需的发布元数据、
OCI 镜像分发、自动化发布和 release 文档，不扩展到新的 MCP tool / resource /
remote deployment 能力：

- 在仓库内补齐符合官方 Registry 要求的 `server.json`
- 选择 `OCI + GHCR + GitHub OIDC` 作为首个官方发布路径
- 让 `distribution/mcp` Docker image 同时支持当前 HTTP 默认启动和 registry 需要的 stdio 启动
- 新增 GitHub Actions 发布流水线，先发布 GHCR image，再发布 MCP Registry metadata
- 更新 MCP quick start / release 文档，说明官方发布入口、镜像启动方式和 release manager 操作
- 使用仓库既有 `specs/` Speckit 结构记录设计，不切换分支

本特性的核心不是立即上线一个公网 remote MCP service，
也不是新增第二种包类型，而是把 “ShardingSphere MCP 可以被官方 Registry 发现和安装”
这条发布链路补齐。

## Problem Statement

当前仓库已经具备 MCP runtime、standalone distribution、Dockerfile 和 CI smoke：

- `distribution/mcp` 可以打出独立运行时
- `distribution/mcp/Dockerfile` 可以构建本地 image
- `mcp/README.md` 已提供 HTTP / stdio 运行说明

但它还不满足官方 MCP Registry 公开发布的关键条件：

1. 缺少 `server.json`，没有标准化 server metadata  
2. Docker image 缺少 `io.modelcontextprotocol.server.name` 验证 annotation  
3. 当前 image 默认只跑 HTTP，无法直接与 Registry 中声明的 `stdio` 本地安装语义对齐  
4. 缺少发布 GHCR image 与 `mcp-publisher publish` 的自动化流水线  
5. release 文档没有把 MCP image / Registry 发布纳入正式流程

这导致 ShardingSphere MCP 即使代码可运行，也无法通过官方 Registry
被下游 marketplace / aggregator 稳定发现。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 仓库提供可验证的官方 Registry 元数据和 OCI 镜像 (Priority: P1)

作为 MCP 维护者，我希望仓库中直接存在符合官方 Registry 规则的 `server.json`
和可验证的 GHCR image 元数据，这样 release 时不需要临时拼装发布信息。

**Why this priority**: 没有标准化元数据和可验证 image，后续自动化发布无从谈起。

**Independent Test**: 读取 `mcp/server.json`、`distribution/mcp/Dockerfile` 和打包资源，
可独立验证 server name、OCI identifier、stdio transport、MCP label 和 stdio config 都齐全。

**Acceptance Scenarios**:

1. **Given** 维护者查看 `mcp/server.json`，**When** 对照官方 schema，
   **Then** 能看到稳定的 server name、repository 元数据、version 和 OCI package 定义。
2. **Given** 官方 Registry 校验 GHCR image，**When** 读取 image annotation，
   **Then** `io.modelcontextprotocol.server.name` 必须与 `server.json.name` 完全一致。
3. **Given** MCP client 通过 OCI package 安装该 server，**When** 按 Registry 元数据启动，
   **Then** image 必须能进入 stdio 模式，而不是只能跑 HTTP。

---

### User Story 2 - release 后能自动发布 GHCR image 和 MCP Registry metadata (Priority: P1)

作为 release manager，我希望发布 GitHub release 或手动触发一次 workflow 后，
仓库自动完成 image 构建、push 和 Registry publish，
这样官方发布不会依赖一次性人工命令。

**Why this priority**: 官方 Registry 发布链路要求先有公开可安装 artifact，再发布 metadata；
如果没有自动化，流程容易漏步骤或产生版本漂移。

**Independent Test**: workflow 文件可独立验证它会解析 release version、
构建 `distribution/mcp`、push 到 GHCR，并执行 `mcp-publisher login github-oidc` 与 `publish`。

**Acceptance Scenarios**:

1. **Given** 发布了 `${RELEASE.VERSION}` 的 GitHub release，**When** workflow 运行，
   **Then** 必须把 `ghcr.io/apache/shardingsphere-mcp:${RELEASE.VERSION}` 推送到 GHCR。
2. **Given** 版本是稳定 semver（如 `5.5.4`），**When** workflow 推镜像，
   **Then** 同时更新 `ghcr.io/apache/shardingsphere-mcp:latest`。
3. **Given** image 发布成功，**When** workflow 执行 `mcp-publisher publish`，
   **Then** 发布使用 GitHub OIDC，而不是额外的长期凭据。

---

### User Story 3 - README 与 release 文档说明官方发布和客户端安装方式 (Priority: P1)

作为使用者和 release manager，我希望仓库文档清楚说明：
ShardingSphere MCP 在哪里发布、如何用镜像跑 HTTP/stdio、release 时如何触发 Registry 发布，
这样使用和维护都不会靠口头知识。

**Why this priority**: 只有代码补齐但文档不更新，发布链路仍然不可持续。

**Independent Test**: 阅读 `mcp/README.md`、`mcp/README_ZH.md` 和 release 文档，
可以独立完成本地 image 启动方式选择和 release 发布核对。

**Acceptance Scenarios**:

1. **Given** 用户阅读 MCP README，**When** 查找官方分发入口，
   **Then** 能看到 Registry server name、GHCR image 和 stdio / HTTP 启动说明。
2. **Given** release manager 阅读 release 文档，**When** 发布 GitHub release，
   **Then** 文档会提示等待 MCP publish workflow 完成并验证 Registry 结果。
3. **Given** 用户想本地用 Docker 作为 MCP package，**When** 按 README 启动，
   **Then** 能通过环境变量切到 stdio 模式而不手写新配置文件。

### Edge Cases

- 不新增公网 remote URL；本轮只发布 package metadata，不伪造 `remotes`。
- 不改变 `distribution/mcp` 现有 HTTP 默认行为；直接 `docker run` 仍保持 HTTP quick start。
- 不把 GitHub release 自动化扩展成 git tag / git push 操作；本轮只补仓库内文件与 workflow。
- 不引入私有 registry 或私有 package 源；官方 Registry 只接收公开可安装制品。
- 不修改无关 MCP runtime 逻辑、tool / resource 行为或 public error surface。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在 `/mcp/server.json` 提供符合官方 MCP Registry schema 的 server metadata。
- **FR-002**: `server.json.name` MUST 使用 GitHub namespace 形式
  `io.github.apache/shardingsphere-mcp`，以匹配 GitHub OIDC 发布路径。
- **FR-003**: `server.json.packages[0]` MUST 使用 `"registryType": "oci"`，
  指向 `ghcr.io/apache/shardingsphere-mcp:<version>` 形式的公开 image。
- **FR-004**: `server.json` MUST 声明 `stdio` transport，
  并通过 package environment variable 表达 image 的 stdio 启动方式。
- **FR-005**: `distribution/mcp/Dockerfile` MUST 添加
  `io.modelcontextprotocol.server.name` annotation，
  且其值与 `server.json.name` 完全一致。
- **FR-006**: `distribution/mcp` image MUST 保持 HTTP 为默认启动方式，
  同时 MUST 支持通过稳定环境变量切换到 stdio 模式。
- **FR-007**: 发布包内 MUST 自带可直接使用的 stdio 配置文件，
  不要求客户端运行时临时生成。
- **FR-008**: 仓库 MUST 提供 GitHub Actions workflow，
  先构建并发布 GHCR image，再使用 `mcp-publisher` 发布 MCP Registry metadata。
- **FR-009**: 该 workflow MUST 支持 GitHub release 触发，
  并 SHOULD 支持 release manager 的手动触发。
- **FR-010**: 该 workflow MUST 使用 GitHub OIDC 登录 MCP Registry，
  不依赖长期保存的 MCP 专用 PAT。
- **FR-011**: workflow MUST 在发布前对 MCP 子链路执行最小必要的构建与验证。
- **FR-012**: README 和 release 文档 MUST 说明官方 Registry server name、
  GHCR image 名称、stdio 启动方式和 release 验证步骤。
- **FR-013**: 本轮实现 MUST 保持不切换分支，不做 git 提交、push 或破坏性操作。
- **FR-014**: 本轮实现 MUST 只修改与 MCP 官方发布、镜像入口、文档和验证相关的文件，
  不夹带无关清理。

### Key Entities *(include if feature involves data)*

- **Registry Server Metadata**: `mcp/server.json` 中定义的官方 MCP Registry 描述，
  包含 name、version、repository、website 和 OCI package。
- **OCI Publication Identity**: `ghcr.io/apache/shardingsphere-mcp:<version>` 及其
  `io.modelcontextprotocol.server.name` annotation。
- **Docker Launch Contract**: 容器默认 HTTP、环境变量切 stdio、
  明确配置文件优先级的启动约定。
- **Registry Publish Workflow**: GitHub Actions 中负责构建 distribution、
  发布 GHCR image 和执行 `mcp-publisher publish` 的流水线。

### Assumptions

- Apache ShardingSphere 的 GitHub 组织仍为 `apache`，
  因而 `io.github.apache/*` 可作为 GitHub OIDC namespace。
- 官方 MCP Registry 在 2026-04-05 仍接受 GHCR 的 `oci` package 类型和 GitHub OIDC 认证。
- GHCR image 以公开可见方式发布，满足官方 Registry “publicly available installation method” 要求。
- `distribution/mcp` 现有 Docker image 仍是首个最小可行公开 artifact，
  本轮不并行引入 npm / PyPI / MCPB 第二发布形态。

## Non-Goals

- 不在本轮上线公网 streamable-http remote deployment。
- 不发布到 Docker Hub；首个标准发布路径只选择 GHCR。
- 不引入自托管 MCP Registry 或私有 registry 聚合。
- 不在本轮实现 `mcp-publisher` 本地开发脚本或 release robot 之外的更多自动化。
- 不修改 `distribution/mcp` 运行时数据库模型、工具集、资源集或协议行为。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 仓库中存在可直接提交官方 Registry 的 `/mcp/server.json`，
  且其 name、package、repository 元数据完整。
- **SC-002**: `distribution/mcp/Dockerfile` 中存在唯一的
  `io.modelcontextprotocol.server.name="io.github.apache/shardingsphere-mcp"` annotation。
- **SC-003**: `distribution/mcp` 包含可直接启动的 `conf/mcp-stdio.yaml`，
  容器可通过环境变量切到 stdio 模式。
- **SC-004**: 仓库存在可执行的 MCP 发布 workflow，
  覆盖 GHCR image publish 与 Registry publish 两段流程。
- **SC-005**: MCP README 与 ShardingSphere release 文档都明确记录了
  官方 Registry / GHCR 发布与验证步骤。

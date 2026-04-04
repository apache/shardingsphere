# Implementation Plan: ShardingSphere MCP Official Registry Publication and OCI Distribution

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-05 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-registry-publication/spec.md)
**Input**: Feature specification from `/specs/012-shardingsphere-mcp-registry-publication/spec.md`

## Summary

本特性把 ShardingSphere MCP 从 “可本地运行” 补齐为 “可被官方 MCP Registry 发现并安装”。

实现策略固定为：

- 用 `mcp/server.json` 描述官方 Registry 元数据
- 用 `ghcr.io/apache/shardingsphere-mcp` 作为首个公开 OCI artifact
- 用 GitHub OIDC 发布到官方 Registry
- 保留 Docker image 的 HTTP 默认行为，同时为 Registry package 明确 stdio 启动路径
- 用中英 README 和 release 文档把发布与使用路径讲清楚

不会切换分支，不会提交 / push，不会扩展到 remote deployment。

## Technical Context

**Language/Version**: Java 17 runtime packaging plus shell / YAML / GitHub Actions / JSON metadata  
**Primary Dependencies**: ShardingSphere MCP distribution, GHCR, official `mcp-publisher` CLI, GitHub OIDC  
**Storage**: repository files only; no new runtime persistence  
**Testing**: JSON / workflow / shell validation, existing MCP packaging smoke, targeted metadata checks  
**Target Platform**: ShardingSphere MCP standalone runtime packaged as OCI image and published to official MCP Registry  
**Project Type**: Java monorepo subproject under `mcp`, `distribution/mcp`, `.github/workflows`, and `docs`  
**Constraints**: no branch switch; no commit / push; keep HTTP default startup; do not invent public remote URLs; keep scope limited to publication assets  
**Scale/Scope**: one new Speckit feature folder, one new `server.json`, one Docker entrypoint extension, one release workflow, and focused doc updates

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  只补官方 Registry 所需的 metadata、image 和文档，不改 MCP runtime 业务能力。
- **Gate 2 - Transparent release path**: PASS  
  `server.json`、image label、workflow 和 release docs 都是 reviewer 可直接审阅的显式资产。
- **Gate 3 - Compatibility preservation**: PASS  
  保持 image 默认 HTTP 行为，避免破坏现有 README quick start。
- **Gate 4 - Official standard alignment**: PASS  
  方案只使用官方 Registry 当前支持的 `oci` package、GitHub namespace 和 GitHub OIDC。
- **Gate 5 - Verification path exists**: PASS  
  可通过 scoped Maven package、JSON 校验、workflow 结构检查和 Dockerfile label 检查验证。

## Project Structure

### Documentation (this feature)

```text
specs/012-shardingsphere-mcp-registry-publication/
├── checklists/
│   └── requirements.md
├── plan.md
├── research.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/server.json
mcp/README.md
mcp/README_ZH.md
distribution/mcp/Dockerfile
distribution/mcp/pom.xml
distribution/mcp/src/main/bin/docker-entrypoint.sh
distribution/mcp/src/main/resources/conf/mcp-stdio.yaml
.github/workflows/mcp-build.yml
.github/workflows/jdk17-subchain-ci.yml
docs/community/content/involved/release/shardingsphere.en.md
docs/community/content/involved/release/shardingsphere.cn.md
```

**Structure Decision**: 不新增模块；Registry metadata 放在 `mcp/`，
Docker launch contract 继续留在 `distribution/mcp`，
release automation 放在现有 GitHub workflows 下。

## Design Decisions

### 1. 首个官方发布形态只选 `oci`

- 不引入 npm / PyPI / MCPB 第二形态
- 直接复用现有 `distribution/mcp/Dockerfile`
- 镜像仓库固定为 `ghcr.io/apache/shardingsphere-mcp`

这样改动最小，也最贴近仓库当前已有的 distribution 资产。

### 2. Registry 名称使用 GitHub namespace

- `server.json.name` 固定为 `io.github.apache/shardingsphere-mcp`
- 发布认证方式固定为 GitHub OIDC

这样不要求额外 DNS / HTTP 域名校验文件，
同时满足官方 Authentication 文档的 namespace 规则。

### 3. 容器默认 HTTP，不牺牲现有 quick start

- 直接 `docker run` 仍走 `conf/mcp.yaml`
- 新增 `conf/mcp-stdio.yaml`
- 新增 `docker-entrypoint.sh`
- 通过 `SHARDINGSPHERE_MCP_TRANSPORT=stdio` 切到 stdio
- 如果显式给出 `SHARDINGSPHERE_MCP_CONFIG` 或命令行参数，则优先使用显式配置

这样 Registry package 的 `stdio` 语义和已有 HTTP quick start 可以共存。

### 4. workflow 先 push image，再 publish metadata

- 构建 / 测试 MCP 子链路
- buildx 推送多架构 GHCR image
- patch `mcp/server.json` 中的 version 与 OCI identifier
- 用 `mcp-publisher login github-oidc`
- 在 `mcp/` 目录执行 publish

这样符合官方“Registry 只托管 metadata，artifact 必须先公开可用”的顺序。

### 5. 稳定版才更新 `latest`

- `5.5.4` 这类稳定 semver：推 `${version}` 与 `latest`
- `5.5.4-RC1` / prerelease：只推 `${version}`

避免候选版覆盖稳定 `latest` 标签。

## Branch Checklist

1. `registry_metadata_exists_and_is_schema_aligned`
   Planned verification: `mcp/server.json` 含 name / version / repository / packages / stdio env 约定
2. `oci_image_is_registry_verifiable`
   Planned verification: Dockerfile 含 `io.modelcontextprotocol.server.name` label，且与 `server.json` 一致
3. `container_can_switch_from_http_to_stdio_without_manual_file_creation`
   Planned verification: distribution 内自带 `conf/mcp-stdio.yaml` 和 `docker-entrypoint.sh`
4. `workflow_publishes_image_before_registry_metadata`
   Planned verification: workflow 中先 build+push GHCR，再执行 `mcp-publisher publish`
5. `release_docs_and_readme_are_updated_together`
   Planned verification: README / README_ZH / release docs 都出现 official Registry 与 workflow 说明
6. `no_branch_switch_and_no_release_side_effects_in_this_turn`
   Planned verification: 只做仓库文件改动，不执行 git branch / commit / push

## Implementation Strategy

1. 先补 Speckit 规格，冻结 `oci + ghcr + github-oidc` 设计。
2. 新增 `mcp/server.json`，固定 server name、repository 和 OCI package 结构。
3. 补 `mcp-stdio.yaml` 和 `docker-entrypoint.sh`，把 image 启动契约拉直。
4. 修改 Dockerfile 和 packaging，使 image annotation 与新入口脚本真正生效。
5. 新增 `mcp-build.yml` workflow，并在现有 MCP CI 中加入 metadata 验证。
6. 更新中英 README 与 release 文档，记录官方发布和验证步骤。
7. 运行最小必要校验，确认 JSON、workflow、shell 和 Maven package 都无明显问题。

## Validation Strategy

- **MCP packaging**
```bash
./mvnw -pl distribution/mcp -am -DskipTests package -B -ntp
```

- **MCP metadata and workflow validation**
```bash
python -m json.tool mcp/server.json >/dev/null
python - <<'PY'
import json
from pathlib import Path
server = json.loads(Path('mcp/server.json').read_text())
assert server['name'] == 'io.github.apache/shardingsphere-mcp'
assert server['packages'][0]['registryType'] == 'oci'
assert server['packages'][0]['transport']['type'] == 'stdio'
PY
```

- **Scoped style / structure check**
```bash
./mvnw -pl distribution/mcp -am -Pcheck -DskipTests checkstyle:check spotless:check
```

- **Static grep verification**
```bash
rg -n "io\\.modelcontextprotocol\\.server\\.name|SHARDINGSPHERE_MCP_TRANSPORT|mcp-publisher login github-oidc|ghcr\\.io/apache/shardingsphere-mcp" \
  /Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp \
  /Users/zhangliang/IdeaProjects/shardingsphere/mcp \
  /Users/zhangliang/IdeaProjects/shardingsphere/.github/workflows
```

## Rollout Notes

- 本轮只补齐仓库资产，不会真的执行发布。
- 如果社区后续决定改用域名 namespace，应另开 follow-up，切换 DNS / HTTP auth。
- 如果未来要同时提供 remote public service，再单独为 `remotes` 增加一轮设计和运维约束。

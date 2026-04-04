# Implementation Plan: ShardingSphere MCP Minimal LLM-Driven E2E Validation

**Branch**: `no-branch-switch-requested` | **Date**: 2026-04-05 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/spec.md)  
**Input**: Feature specification from `/specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/spec.md`

## Summary

本特性为 ShardingSphere MCP 补一条真正的 “模型驱动” 验收路径：

- 使用仓库自带的打包 MCP distribution
- 使用仓库自带的 H2 demo runtime 作为真实数据库基线
- 使用本地自托管小模型服务
- 通过 repository-owned 的薄 runner 驱动真实模型做 MCP tool calling
- 在 GitHub Actions 中完成只读 smoke 与 artifact 留存

这条 lane 是新增的上层验证，不替代现有 deterministic MCP E2E 与 distribution smoke。
本轮不切换分支，不修改其他 Speckit 目录，只在新的隔离 spec 目录中落设计文档。

## Technical Context

**Language/Version**: Java 17 for the repository-owned test runner, plus GitHub Actions workflow shell orchestration  
**Primary Dependencies**: existing `test/e2e/mcp` module, packaged MCP distribution, local HTTP-accessible model service, Jackson/Java `HttpClient` already available in the test stack  
**Storage**: demo H2 data under packaged MCP runtime, per-run artifacts under `test/e2e/mcp/target/llm-e2e/<run-id>/`  
**Testing**: targeted `test/e2e/mcp` JUnit smoke, workflow-level orchestration, existing deterministic MCP E2E kept intact  
**Target Platform**: GitHub-hosted Linux runner without GPU, plus local developer reproduction on the same contract  
**Project Type**: Java monorepo subproject under `test/e2e/mcp`, `.github/workflows`, and MCP operator docs  
**Constraints**: no branch switch; no external SaaS model secret in default path; first slice read-only only; no replacement of deterministic E2E; artifact directories must be isolated per run  
**Scale/Scope**: one minimal smoke scenario, one default model profile, one dedicated workflow, one local quickstart path

## Constitution Check

*GATE: Must pass before implementation starts. Re-check after design.*

- **Gate 1 - Smallest safe change**: PASS  
  新增一条独立的 LLM smoke lane，不改现有 deterministic MCP contract 测试主路径。
- **Gate 2 - Explicit governance and security**: PASS  
  第一阶段限定只读 SQL、固定 JSON contract 和 artifact trace，避免不受控写操作。
- **Gate 3 - Testable delivery**: PASS  
  有独立的 JUnit smoke、workflow orchestration、artifact 断言和本地 quickstart。
- **Gate 4 - Traceable contracts**: PASS  
  模型输出、tool 覆盖、artifact、资源边界和 workflow 策略都在 spec / plan / contract / tasks 中可追踪。
- **Gate 5 - Quality gates**: PASS  
  已定义 `test/e2e/mcp` 的 scoped test 与 style gate 命令。

## Project Structure

### Documentation (this feature)

```text
specs/012-shardingsphere-mcp-llm-e2e-ci-20260405/
├── checklists/
│   └── requirements.md
├── contracts/
│   └── llm-e2e-acceptance-contract.md
├── data-model.md
├── plan.md
├── quickstart.md
├── research.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
.github/workflows/mcp-llm-e2e.yml
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EConfiguration.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLME2EArtifactBundle.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMStructuredAnswer.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/MCPToolTraceRecord.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMChatModelClient.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/LLMMCPConversationRunner.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/AbstractLLMMCPE2ETest.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/ProductionLLMSmokeE2ETest.java
test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/*Test.java
test/e2e/mcp/src/test/resources/llm/minimal-smoke-system-prompt.md
test/e2e/mcp/src/test/resources/llm/minimal-smoke-user-prompt.md
test/e2e/mcp/src/test/resources/llm/expected/minimal-smoke-response.json
mcp/README.md
mcp/README_ZH.md
```

**Structure Decision**: 不新增新的 top-level Maven module；
优先把 LLM runner 作为 `test/e2e/mcp` 下的独立子包落地，
这样能最大化复用现有 MCP E2E harness、distribution 打包路径和 GitHub Actions 构建上下文。

## Design Decisions

### 1. LLM E2E 是增量上层验证，不替换 deterministic MCP E2E

- 继续保留现有：
  - protocol-level deterministic E2E
  - production-runtime E2E
  - distribution smoke
- 新增 LLM lane 只负责证明：
  - 真实模型真的会调用 MCP
  - 真实模型真的能经由 MCP 读到数据库

### 2. 第一阶段验收固定绑定打包 demo H2 runtime

- 直接复用 `distribution/mcp/conf/mcp.yaml` 的 demo H2 `runtimeDatabases`
- smoke contract 固定验证 `orders.public.orders`
- 这样避免把第一版成本扩展到外部数据库编排

### 3. 使用 repository-owned 的薄 Java runner，而不是外部 agent framework

- runner 负责：
  - 调本地模型服务
  - 驱动 MCP session 与 tool 调用闭环
  - 记录 tool trace
  - 断言最终 JSON
- 不在第一阶段引入 Goose、Qwen-Agent 之类通用 agent framework，
  以减少依赖、资源开销和诊断复杂度

### 4. 模型服务接入采用可配置的本地 HTTP chat profile

- runner 通过配置接入本地模型服务
- 默认 smoke 只固定一个最小模型 profile
- 代码层避免把 workflow 直接耦死到某个 provider brand

### 5. 最终验收只看结构化 JSON 和 trace，不看 prose

- 必须有固定 JSON schema
- 必须有 required tool coverage
- 必须有只读 SQL 审计
- raw text 仅作为 artifact，不作为通过标准

### 6. workflow 先独立，再决定是否升级 gate

- 第一轮以 `workflow_dispatch` + `schedule` 交付
- 不直接塞进当前 `jdk17-subchain-ci.yml`
- 等稳定后再评估是否纳入更强 gate

### 7. 每次 run 使用唯一 artifact 目录

- artifact root 统一放在 `test/e2e/mcp/target/llm-e2e/<run-id>/`
- `<run-id>` 使用时间戳或 UUID
- 解决本地并发与 CI 重试时的覆盖问题

## Branch Checklist

1. `model_must_use_real_mcp_tool_calls`
   Planned verification: `ProductionLLMSmokeE2ETest` 断言 trace 同时包含 discovery tool 与 `execute_query`
2. `smoke_contract_must_stay_read_only_and_deterministic`
   Planned verification: runner 拒绝非只读 SQL，最终 JSON 断言 `orders.public.orders -> total_orders=2`
3. `failure_diagnostics_must_be_complete`
   Planned verification: artifact bundle 包含 prompt、raw output、tool trace、assertion report、MCP log
4. `workflow_must_be_resource_bounded_and_independent`
   Planned verification: 新 workflow 独立存在，不替换现有 MCP smoke lane
5. `local_repro_must_match_ci_contract`
   Planned verification: quickstart 与 targeted Maven 命令复现同一 smoke 场景

## Implementation Strategy

1. 先新增 LLM smoke 所需的配置模型、artifact 模型和 tool trace 模型。
2. 再落 repository-owned 模型客户端和 MCP conversation runner。
3. 基于打包 MCP distribution 增加 `ProductionLLMSmokeE2ETest`。
4. 新增 GitHub Actions workflow，负责启动模型服务、预热、运行测试和上传 artifacts。
5. 更新中英文 README 和 quickstart，保证本地复现路径清晰。

## Validation Strategy

- **Targeted LLM smoke verification**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionLLMSmokeE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped helper tests**
```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=LLME2EConfigurationTest,LLMChatModelClientTest,LLMMCPConversationRunnerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

- **Scoped style checks**
```bash
./mvnw -pl test/e2e/mcp -am -Pcheck -DskipITs -DskipTests \
  checkstyle:check spotless:check
```

- **Workflow contract sanity**
```bash
rg -n "mcp-llm-e2e|workflow_dispatch|schedule|upload-artifact" \
  /Users/zhangliang/IdeaProjects/shardingsphere/.github/workflows
```

- **Artifact isolation sanity**
```bash
rg -n "target/llm-e2e" \
  /Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp
```

## Rollout Notes

- 第一版只接受最小 read-only smoke，不接受 open-ended agent 场景蔓延。
- 如果 hosted-runner 稳定性不足，优先保留 nightly / manual lane，而不是回退到伪模型。
- 如果后续需要验证客户真实数据库拓扑，应另开 follow-up spec，
  在此 LLM smoke 之上扩展新的 runtime profile，而不是污染第一阶段最小 contract。

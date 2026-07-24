+++
pre = "<b>6.5. </b>"
title = "MCP E2E 测试"
weight = 5
chapter = true
+++

本章说明 ShardingSphere-MCP 的端到端契约验证和 LLM usability 验证。

## 范围

`test/e2e/mcp` 覆盖：

- 发行包启动和配置。
- 基于真实 MySQL、PostgreSQL 和 Proxy 的 HTTP、STDIO runtime。
- Tool、resource、prompt 和 completion 的跨进程发现与执行。
- 适用于当前 server capability 的官方 MCP conformance 场景。
- 真实模型驱动的 MCP usability。
- 基于实时元数据和数据的 10 题自主 MCP Builder 评测。
- Encrypt、Mask、Broadcast、Readwrite-Splitting、Shadow 和 Sharding workflow 验收。

不需要 Docker 的 HTTP 协议、会话和安全边界由 `mcp/bootstrap` 的 `StreamableHttpMCPServerIT` 覆盖，不属于 E2E。

## Feature 模板验收

当某个 MCP feature 被作为 workflow 模板时，E2E 测试应覆盖协议可发现性、模型可用性和负向契约。
以 Encrypt workflow 为例，模板级验收至少包括：

- Completion 能返回 feature 可用的算法或候选值。
- Plan 输出只包含当前 feature 支持的 DistSQL artifact。
- Plan 输出不包含不支持的物理 DDL、索引、迁移、回填或清理 artifact。
- `resources_to_read` 指向 feature 自有算法、规则或配置资源，而不是不属于该 feature 的物理元数据资源。
- Descriptor output schema 不暴露当前 feature 不支持的输出字段。
- 计划、workflow resource、preview、apply、validate、recovery 和 trace 可见输出不泄露敏感参数。
- 自定义或能力未知的算法应被标记为未确认，而不是被当作已知能力处理。
- Drop 场景应验证规则删除语义，不把物理清理作为成功条件。
- Encrypt、Mask 和 Sharding 的 ALTER 扩展、物理 DDL、迁移和回填仍是排除在外的商业版本能力。
- Apply 必须经过 preview，并校验用户批准的步骤。

测试复用应保留在 `test/e2e/mcp` 内的本地 helper 中；不要为了模板验收新增测试 jar 或跨模块测试支撑模块。

## 本地准备

构建并安装 MCP E2E 依赖和 distribution：

```bash
./mvnw -pl test/e2e/mcp,distribution/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

构建本地 distribution image：

```bash
docker build --platform "$(docker version --format '{{.Server.Os}}/{{.Server.Arch}}')" -f distribution/mcp/Dockerfile -t apache/shardingsphere-mcp-e2e:local distribution/mcp/target
```

## LLM Runtime

MCP LLM lane 默认使用本地 Docker image 承载 OpenAI-compatible endpoint。
构建前建议先查看 Docker 占用：

```bash
docker system df
```

只校验本机架构选择，不下载模型：

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh --dry-run
```

构建本地 runtime image：

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh
```

## 运行 MCP Runtime E2E

MCP E2E 运行配置集中在 `test/e2e/mcp/src/test/resources/env/e2e-env.properties`。
本地运行时可以直接修改该文件，也可以使用同名 `-D` 系统参数覆盖。

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp
```

## 运行 MCP HTTP IT

该测试启动真实 HTTP server，但不连接 Docker、数据库或模型：

```bash
./mvnw -pl mcp/bootstrap verify
```

## 运行 LLM Usability Suite

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp.llm -Dtest=LLMUsabilitySuiteE2ETest
```

## 运行自主 MCP Builder 评测

评测从 `llm/evaluation/mcp-builder-evaluation.xml` 加载 10 个相互独立的只读问题。
每个问题都创建新的 MCP session，从实时 `tools/list` response 派生模型 function definitions，保留模型原始 response 和 MCP structured response，并且不注入纠错提示或期望答案，只对最终答案做精确比较。
用于闭合评分的运行显式使用 32768 token context window。选中 `llm-e2e` lane 后，如果 Docker、模型、数据库或 MCP 基础设施缺失，测试直接失败，不把评分失败转换成 skip。

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp.llm -Dtest=MCPBuilderEvaluationE2ETest
```

## 官方 MCP Conformance

CI conformance lane 将 `modelcontextprotocol/conformance` 固定在 commit `21a9a2febd7100d7c17ac1021ee7f2ed9f66a1e0`，传入 protocol version `2025-11-25`，并且只运行 workflow 中声明的适用通用 server 场景。
上游固定使用 `test_*` tool/resource 的产品无关调用、未声明的可选能力和固定 HTTP 传输面以外的场景不适用于本项目，产品能力继续由确定性 E2E 覆盖；不会为了上游 fixture 添加生产测试钩子。
打包后的 server 使用 loopback HTTP 配置运行，使 DNS rebinding 场景校验 loopback Origin 策略，而不是独立的 Docker 远程绑定策略。

## External Debug

仅本地调试时，可以连接已经运行的 OpenAI-compatible endpoint：

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp.llm -Dtest=LLMUsabilitySuiteE2ETest -Dmcp.llm.runtime-mode=external-debug -Dmcp.llm.base-url=http://127.0.0.1:8080/v1
```

External debug endpoint 不能作为 score-closing evidence。

## 产物

LLM usability 和 MCP Builder evaluation artifact 写入：

```text
test/e2e/mcp/target/llm-e2e/
```

每个自主评测 case 都记录问题、期望与实际答案、原始模型 response、MCP interaction trace、实时 tool definitions、runtime evidence 和 assertion report。Artifact 写入会脱敏 secret-shaped 值；如果发现未脱敏 secret pattern 或已知模型 API key，评分运行直接失败。

GitHub Actions 入口：

- `.github/workflows/e2e-mcp.yml`

这条 workflow 是 MCP runtime E2E 的必跑入口。
如果超大 PR 因 path filter 限制漏触发，可以使用 `workflow_dispatch` 手动补充 evidence。

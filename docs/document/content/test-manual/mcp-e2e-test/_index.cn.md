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
- HTTP runtime。
- STDIO runtime。
- MCP baseline contract。
- Tool/resource/prompt/completion discovery。
- 真实模型驱动的 MCP usability。
- Encrypt 和 Mask workflow 可用性验证。

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
- 不支持的 alter 扩展应返回清晰限制，而不是生成不完整 workflow。
- Apply 必须经过 preview，并校验用户批准的步骤。

测试复用应保留在 `test/e2e/mcp` 内的本地 helper 中；不要为了模板验收新增测试 jar 或跨模块测试支撑模块。

## 本地准备

先安装 MCP E2E 依赖模块到本地仓库：

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

打包 MCP distribution 并构建本地 distribution image：

```bash
./mvnw -pl distribution/mcp -am -DskipTests package -B -ntp
docker build -f distribution/mcp/Dockerfile -t apache/shardingsphere-mcp-e2e:local distribution/mcp/target
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
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest='*E2ETest' \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -De2e.run.type=DOCKER \
  -Dmcp.e2e.container.image=apache/shardingsphere-mcp-e2e:local
```

## 运行 LLM Usability Suite

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -De2e.run.type=DOCKER
```

## External Debug

仅本地调试时，可以连接已经运行的 OpenAI-compatible endpoint：

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -De2e.run.type=DOCKER \
  -Dmcp.llm.runtime-mode=external-debug \
  -Dmcp.llm.base-url=http://127.0.0.1:8080/v1 \
  -Dsurefire.failIfNoSpecifiedTests=true
```

External debug endpoint 不能作为 score-closing evidence。

## 产物

LLM artifact 写入：

```text
test/e2e/mcp/target/llm-e2e/
```

GitHub Actions 入口：

- `.github/workflows/e2e-mcp.yml`

这条 workflow 是 MCP runtime E2E 的必跑入口。
如果超大 PR 因 path filter 限制漏触发，可以使用 `workflow_dispatch` 手动补充 evidence。

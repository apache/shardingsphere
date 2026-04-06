# Quickstart: ShardingSphere MCP LLM Usability Evaluation

## Goal

本 quickstart 定义的是后续实现完成后的目标使用方式，
用于本地复现最小 usability baseline。

## Prerequisites

- JDK 17
- Docker（用于 MySQL baseline）
- 本地可访问的模型服务
- 现有 `test/e2e/mcp` 依赖能够正常运行

## Intended Execution Flow

1. 启动或确保本地模型服务可用
2. 设置最小 evaluation profile 所需环境变量
3. 运行 minimal usability suite
4. 查看 `scorecard.json` 和 `summary.md`
5. 如需比较，与上一次 baseline run 做 regression compare

## Intended Environment Variables

- `MCP_LLM_E2E_ENABLED=true`
- `MCP_LLM_BASE_URL=http://127.0.0.1:11434/v1`
- `MCP_LLM_MODEL=qwen3:1.7b`
- `MCP_LLM_ARTIFACT_ROOT=target/llm-usability`
- `MCP_LLM_RUN_ID=<custom-run-id>`
- `MCP_LLM_USABILITY_PROFILE=minimal`

## Intended Commands

### 1. Run the minimal baseline

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

### 2. Run the existing lower-layer LLM smoke first if needed

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionLLMH2SmokeE2ETest,ProductionLLMMySQLSmokeE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

### 3. Run comparison mode

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilityRegressionCheckerTest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

## Expected Artifacts

每次 run 应写入独立目录，例如：

```text
target/llm-usability/<run-id>/
├── scorecard.json
├── summary.md
├── scenario-results.json
├── traces/
└── raw-output/
```

## Expected First Questions the Suite Should Answer

- 模型是否会优先命中正确 resource
- 模型是否会在 `list_tables`、`describe_table`、`search_metadata` 间反复试错
- 模型是否能在少量往返中完成 H2 / MySQL 的 discovery + query
- 一次错误后是否能恢复

## Notes

- 第一阶段建议手工或 nightly 运行，而不是直接做主 PR gate。
- 如果 lower-layer smoke 都不稳定，不应先怀疑 usability scorecard，
  而应先排查模型服务、runtime、seed data 和既有 LLM smoke。

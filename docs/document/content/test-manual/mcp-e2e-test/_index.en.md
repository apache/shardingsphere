+++
pre = "<b>6.5. </b>"
title = "MCP E2E Test"
weight = 5
chapter = true
+++

This chapter describes ShardingSphere-MCP end-to-end contract validation and LLM usability validation.

## Scope

`test/e2e/mcp` covers:

- Distribution startup and configuration.
- HTTP runtime.
- STDIO runtime.
- MCP baseline contract.
- Tool/resource/prompt/completion discovery.
- Real-model MCP usability.
- Encrypt and Mask workflow usability validation.

## Local preparation

Install MCP E2E dependency modules into the local repository first:

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

## LLM Runtime

The MCP LLM lane uses a local Docker image to host an OpenAI-compatible endpoint.
Before building, inspect Docker usage:

```bash
docker system df
```

Check host architecture selection without downloading the model:

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh --dry-run
```

Build the local runtime image:

```bash
sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh
```

## Run MCP Runtime E2E

```bash
MCP_E2E_TESTS=HttpTransportContractE2ETest,HttpTransportProtocolContractE2ETest,HttpTransportBaselineContractE2ETest
MCP_E2E_TESTS="${MCP_E2E_TESTS},ProductionMySQLRuntimeE2ETest"
MCP_E2E_TESTS="${MCP_E2E_TESTS},HttpProductionProxyEncryptWorkflowE2ETest"
MCP_E2E_TESTS="${MCP_E2E_TESTS},HttpProductionProxyMaskWorkflowE2ETest,LLMUsabilitySuiteE2ETest"
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest="${MCP_E2E_TESTS}" \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -Dmcp.e2e.contract.enabled=true \
  -Dmcp.e2e.production.mysql.enabled=true \
  -Dmcp.e2e.production.stdio.enabled=true \
  -Dmcp.e2e.llm.enabled=true \
  -Dmcp.e2e.llm.excludedGroups=
```

## Run LLM Usability Suite

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

## External Debug

For local debugging only, connect to an already running OpenAI-compatible endpoint:

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dmcp.llm.runtime-mode=external-debug \
  -Dmcp.llm.base-url=http://127.0.0.1:8080/v1 \
  -Dsurefire.failIfNoSpecifiedTests=true
```

External debug endpoints cannot be used as score-closing evidence.

## Artifacts

LLM artifacts are written under:

```text
test/e2e/mcp/target/llm-e2e/
```

GitHub Actions entry points:

- `.github/workflows/mcp-e2e.yml`

This workflow is the mandatory MCP runtime E2E entry point.
If a very large PR misses a path-filter match, use `workflow_dispatch` to add manual evidence.

+++
pre = "<b>6.5. </b>"
title = "MCP E2E Test"
weight = 5
chapter = true
+++

This chapter describes the ShardingSphere-MCP Functionality, Conformance, and LLM end-to-end test suites.

## Scope

The MCP E2E workflow contains three independent test suites:

- MCP Functionality E2E validates distribution startup and configuration, HTTP and STDIO runtimes backed by real MySQL, PostgreSQL, and Proxy processes, cross-process discovery and execution of tools, resources, prompts, and completions, and the Encrypt, Mask, Broadcast, Readwrite-Splitting, Shadow, and Sharding workflows.
- MCP Conformance E2E uses the official runner to validate MCP protocol scenarios applicable to the published server capabilities.
- MCP LLM E2E validates that a real model can use MCP over HTTP or STDIO for read-only queries, metadata discovery, resource navigation, side-effect previews, and error recovery.

The `StreamableHttpMCPServerIT` in `mcp/bootstrap` covers HTTP protocol, session, and security boundaries that require neither Docker nor an external service.

## Feature template acceptance

When an MCP feature is used as a workflow template, E2E tests should cover protocol discoverability, model usability, and negative contracts.
For the Encrypt workflow, template-level acceptance should include at least:

- Completion returns feature-available algorithms or candidate values.
- Plan output contains only DistSQL artifacts supported by the current feature.
- Plan output does not contain unsupported physical DDL, index, migration, backfill, or cleanup artifacts.
- `resources_to_read` points to feature-owned algorithm, rule, or configuration resources, not physical metadata resources outside the feature.
- Descriptor output schema does not expose output fields unsupported by the current feature.
- Plan, workflow resource, preview, apply, validate, recovery, and trace-visible outputs do not leak sensitive properties.
- Custom algorithms or algorithms with unknown capabilities are marked as unconfirmed instead of being treated as known-capability algorithms.
- Drop scenarios validate rule removal semantics and do not use physical cleanup as a success condition.
- Encrypt, Mask, and Sharding ALTER expansion, physical DDL, migration, and backfill remain excluded commercial-edition capabilities.
- Apply must be preceded by preview and must validate user-approved steps.

Test reuse should stay in local helpers under `test/e2e/mcp`; do not add a test jar or a cross-module test-support module for template acceptance.

## Local preparation

Build and install the MCP E2E dependencies and distribution:

```bash
./mvnw -pl test/e2e/mcp,distribution/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

Build the local distribution image:

```bash
docker build --platform "$(docker version --format '{{.Server.Os}}/{{.Server.Arch}}')" -f distribution/mcp/Dockerfile -t apache/shardingsphere-mcp-e2e:local distribution/mcp/target
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

## Run MCP Functionality E2E

MCP E2E runtime configuration is centralized in `test/e2e/mcp/src/test/resources/env/e2e-env.properties`.
For local runs, edit that file or override the same keys with `-D` system properties.

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp.functionality
```

## Run MCP HTTP IT

This test starts the real HTTP server without connecting to Docker, a database, or a model:

```bash
./mvnw -pl mcp/bootstrap verify
```

## Run MCP LLM E2E

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp.llm
```

`LLMHttpE2ETest` covers the HTTP scenarios, and `LLMStdioE2ETest` covers an autonomous read-only query over STDIO. Each scenario uses the live `tools/list` response and preserves the model response, structured MCP response, interaction trace, and assertion report. Missing Docker, model, database, or MCP infrastructure fails the selected `llm-e2e` lane instead of converting the failure into a skipped case.

## MCP Conformance E2E

The CI conformance lane pins `modelcontextprotocol/conformance` to commit `21a9a2febd7100d7c17ac1021ee7f2ed9f66a1e0`, passes protocol version `2025-11-25`, and executes only the applicable generic server scenarios declared in the workflow.
Upstream calls tied to fixed `test_*` tools or resources, unadvertised optional capabilities, and scenarios outside the fixed HTTP transport surface do not apply to this project. Product capabilities remain covered by deterministic E2E tests, and no production test hooks are added for upstream fixtures.
The packaged server runs with its loopback HTTP configuration so the DNS rebinding scenario validates the loopback Origin policy rather than the separate Docker remote-binding policy.

## External Debug

For local debugging only, connect to an already running OpenAI-compatible endpoint:

```bash
./mvnw -pl test/e2e/mcp test -Pe2e.mcp.llm -Dtest=LLMHttpE2ETest -Dmcp.llm.runtime-mode=external-debug -Dmcp.llm.base-url=http://127.0.0.1:8080/v1
```

External debug endpoints cannot be used as score-closing evidence.

## Artifacts

MCP LLM E2E artifacts are written under:

```text
test/e2e/mcp/target/llm-e2e/
```

Each scenario records the question, expected and actual answer, raw model response, MCP interaction trace, live tool definitions, runtime evidence, and assertion report. Artifact writing redacts secret-shaped values, and the test fails if an unredacted secret pattern or the known model API key is present.

GitHub Actions entry points:

- `.github/workflows/e2e-mcp.yml`

This workflow is the shared entry point for all three MCP E2E suites.
If a very large PR misses a path-filter match, use `workflow_dispatch` to add manual evidence.

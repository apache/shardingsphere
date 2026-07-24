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
- Official MCP conformance scenarios applicable to the published server capabilities.
- Real-model MCP usability.
- Autonomous 10-question MCP Builder evaluation against live metadata and data.
- Encrypt, Mask, Broadcast, Readwrite-Splitting, Shadow, and Sharding workflow validation.

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

Install MCP E2E dependency modules into the local repository first:

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

Package the MCP distribution and build the local distribution image:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package -B -ntp
docker build -f distribution/mcp/Dockerfile -t apache/shardingsphere-mcp-e2e:local distribution/mcp/target
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

MCP E2E runtime configuration is centralized in `test/e2e/mcp/src/test/resources/env/e2e-env.properties`.
For local runs, edit that file or override the same keys with `-D` system properties.

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest='*E2ETest' \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -De2e.run.type=DOCKER \
  -Dmcp.e2e.container.image=apache/shardingsphere-mcp-e2e:local
```

## Run LLM Usability Suite

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -De2e.run.type=DOCKER
```

## Run Autonomous MCP Builder Evaluation

The evaluation loads ten independent read-only questions from `llm/evaluation/mcp-builder-evaluation.xml`.
Each question starts a fresh MCP session, derives model function definitions from the live `tools/list` response, preserves raw model responses and structured MCP responses, and compares the final answer exactly without correction prompts or expected-answer injection.
Score-closing runs use an explicit 32768-token context window. Missing Docker, model, database, or MCP infrastructure fails the selected `llm-e2e` lane instead of converting a score failure into a skipped case.

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=MCPBuilderEvaluationE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  -De2e.run.type=DOCKER
```

## Official MCP Conformance

The CI conformance lane pins `modelcontextprotocol/conformance` to commit `21a9a2febd7100d7c17ac1021ee7f2ed9f66a1e0`, passes protocol version `2025-11-25`, and executes only the generic server scenarios listed in
`test/e2e/mcp/src/test/resources/conformance/server-scenarios.txt`. Product-specific calls remain covered by deterministic E2E tests because the upstream fixtures use `test_*` tools and resources that this server does not publish.

## External Debug

For local debugging only, connect to an already running OpenAI-compatible endpoint:

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -De2e.run.type=DOCKER \
  -Dmcp.llm.runtime-mode=external-debug \
  -Dmcp.llm.base-url=http://127.0.0.1:8080/v1 \
  -Dsurefire.failIfNoSpecifiedTests=true
```

External debug endpoints cannot be used as score-closing evidence.

## Artifacts

LLM usability and MCP Builder evaluation artifacts are written under:

```text
test/e2e/mcp/target/llm-e2e/
```

Each autonomous evaluation case records the question, expected and actual answer, raw model response, MCP interaction trace, live tool definitions, runtime evidence, and assertion report. Artifact writing redacts secret-shaped values, and scoring runs fail if an unredacted secret pattern or the known model API key is present.

GitHub Actions entry points:

- `.github/workflows/e2e-mcp.yml`

This workflow is the mandatory MCP runtime E2E entry point.
If a very large PR misses a path-filter match, use `workflow_dispatch` to add manual evidence.

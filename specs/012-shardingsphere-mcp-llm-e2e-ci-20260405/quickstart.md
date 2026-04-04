# Quickstart: ShardingSphere MCP Minimal LLM-Driven E2E Validation

## Purpose

Provide the expected maintainer flow for validating that a real local model can use MCP to discover the demo database and execute one read-only query against the packaged MCP distribution.

## Prerequisites

- JDK 17 toolchain available from the repository root
- Maven wrapper available
- Docker available for the local model service
- A local HTTP-accessible model service exposing one small tool-capable profile
- No external model API key required for the default smoke path

## 1. Build the packaged MCP distribution

From the repository root:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

Expected outcome:

- The packaged MCP distribution is produced under `distribution/mcp/target/`
- The packaged `conf/mcp.yaml` keeps the demo `orders` and `billing` H2 runtime

## 2. Start the local model service

Start the chosen local model service so it exposes a local chat endpoint reachable from the test runner.

Operational expectations:

- The model profile must be small enough for CPU-only smoke usage
- The model must support tool-calling-compatible chat behavior
- The service must be warmed up before running the smoke test

## 3. Export the local model environment

Set the environment variables expected by the LLM smoke runner:

```bash
export MCP_LLM_BASE_URL=http://127.0.0.1:<model-port>/v1
export MCP_LLM_MODEL=<small-model-name>
export MCP_LLM_API_KEY=dummy
```

If the chosen local service does not require a key,
the placeholder value still keeps the runner contract consistent.

## 4. Run the model-driven smoke test

From the repository root:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionLLMSmokeE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

Expected smoke behavior:

1. The test starts the packaged MCP runtime
2. The runner waits for the MCP HTTP endpoint to become ready
3. The runner drives the model through discovery
4. The model uses MCP tools to find `orders.public.orders`
5. The model uses `execute_query` to verify `SELECT COUNT(*) AS total_orders FROM orders`
6. The runner validates the final structured JSON

## 5. Inspect the artifacts

Each run writes artifacts into an isolated directory:

```text
test/e2e/mcp/target/llm-e2e/<run-id>/
```

Expected contents:

- system prompt
- user prompt
- raw model output
- tool trace
- assertion report
- MCP runtime log

## 6. Expected passing result

A passing run must prove all of the following:

1. The model used at least one discovery tool
2. The model used `execute_query`
3. The final JSON identifies `orders.public.orders`
4. The final JSON reports `totalOrders = 2`

If any one of these is missing, the smoke run is not considered valid.

## 7. Workflow usage

The corresponding GitHub Actions workflow is intended to:

1. Start the local model service on the runner
2. Warm the model
3. Run the same `ProductionLLMSmokeE2ETest`
4. Upload the isolated artifact directory

The workflow should remain separate from the existing deterministic MCP CI lane during the first rollout.

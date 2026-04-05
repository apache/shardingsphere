# Quickstart: ShardingSphere MCP Minimal LLM-Driven E2E Validation

## Purpose

Provide the expected maintainer flow for validating that a real local model can use MCP to discover seeded tables and execute one read-only query against the production bootstrap runtime harness used by `test/e2e/mcp`.

## Prerequisites

- JDK 17 toolchain available from the repository root
- Maven wrapper available
- A local HTTP-accessible Ollama service exposing `qwen3:1.7b`
- No external model API key required for the default smoke path
- Docker available for the MySQL smoke path

## 1. Start the local model service

Start Ollama so it exposes a local OpenAI-compatible chat endpoint reachable from the test runner.

Operational expectations:

- Pull `qwen3:1.7b`
- The model profile must be small enough for CPU-only smoke usage
- The model must support tool-calling-compatible chat behavior
- The service must be warmed up before running the smoke test

## 2. Export the local model environment

Set the environment variables expected by the LLM smoke runner:

```bash
export MCP_LLM_BASE_URL=http://127.0.0.1:<model-port>/v1
export MCP_LLM_MODEL=qwen3:1.7b
export MCP_LLM_API_KEY=ollama
```

If the chosen local service does not require a key,
the placeholder value still keeps the runner contract consistent.

## 3. Run the model-driven smoke tests

From the repository root:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionLLMH2SmokeE2ETest,ProductionLLMMySQLSmokeE2ETest test \
  -Dsurefire.failIfNoSpecifiedTests=false
```

Expected smoke behavior:

1. The tests start the production bootstrap runtime in-process over HTTP
2. The runner waits for the MCP HTTP endpoint to become ready
3. The runner drives the model through discovery
4. The model uses MCP tools to find `logic_db.public.orders` for the H2 path
5. The model uses `execute_query` to verify `SELECT COUNT(*) AS total_orders FROM orders`
6. The runner validates the final structured JSON
7. The runner repeats the same contract against the MySQL Docker runtime

## 4. Inspect the artifacts

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
- MCP interaction log

## 5. Expected passing result

A passing run must prove all of the following:

1. The model used `list_tables`, `describe_table`, and `execute_query` in order
2. The model used `execute_query` to verify the final row count rather than guessing
3. The H2 final JSON identifies `logic_db.public.orders`
4. The H2 final JSON reports `totalOrders = 2`
5. The MySQL final JSON identifies `logic_db.<detected-schema>.orders` and reports `totalOrders = 2`

If any one of these is missing, the smoke run is not considered valid.

## 6. Workflow usage

The corresponding GitHub Actions workflow is intended to:

1. Start the local model service on the runner
2. Warm the model
3. Run the same `ProductionLLMH2SmokeE2ETest` and `ProductionLLMMySQLSmokeE2ETest`
4. Upload the isolated artifact directory

The workflow should remain separate from the existing deterministic MCP CI lane during the first rollout.

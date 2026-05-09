<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# MCP LLM Product Quality 100 Requirements

## Purpose

This inventory mirrors `.specify/specs/011-mcp-llm-product-quality-100/` for the strict product-quality target.

Current strict score: **78/100**.
Target score: **100/100**.

The score includes model friendliness, natural interaction, semantic clarity, code readability,
architecture clarity, decoupling, contract stability, recovery, safety, test credibility,
evolvability, and operations readiness.

## Confirmed Decisions

- The mandatory live LLM E2E gate uses Dockerized Ollama with Alibaba Qwen model id `qwen3:1.7b`.
- All live MCP LLM E2E tests for the 100-point gate use this model.
- MCP E2E owns Dockerized Ollama startup and model pull.
- MCP E2E must automatically pull `qwen3:1.7b` when absent.
- The mandatory live gate uses a local Ollama OpenAI-compatible endpoint and requires no paid external API key.
- Multi-provider or multi-model runs are optional evidence.
- Natural LLM scenarios may be rewritten completely.
- Backward compatibility is not required.
- Golden contracts use JSON snapshots plus focused schema/assertion helpers.
- Packaged HTTP diagnostics are mandatory.
- Packaged STDIO diagnostics are covered where practical.
- `preview` is always allowed.
- Real side effects require an explicit approval signal.
- LLM usability scenarios default to preview, manual-only, and approval-violation checks.
- Extended LLM scenarios are non-blocking only for model-performance outcomes.
- Deterministic extended checks remain hard assertions.

## P0 Requirements

- Stay on branch `001-shardingsphere-mcp`.
- Separate design-clarity completion from strict product-quality scoring.
- Replace scripted LLM usability tasks with natural user-intent tasks.
- Keep protocol-contract tests separate when exact first calls are required.
- Add full-score metrics for task success, first correct action, invalid calls, round trips, recovery success, approval violations, and answer fidelity.
- Split core blocking scenarios from extended scored scenarios.
- Core scenarios must fail when any scored assertion misses the full-score gate.
- Extended scenarios must record model-performance misses without failing the suite.
- Extended scenarios must fail for deterministic failures such as artifact loss, invalid score shape,
  trace schema errors, secret leakage, contract drift, runtime setup failure, and unblocked side effects.
- Add golden contract protection for capabilities, tools, resources, prompts, completions, errors, and workflow payloads.
- Refine recovery categories and make the primary next path unambiguous.
- Prove missing-context, unsupported-resource, invalid-enum, SQL-tool-mismatch, and stale-workflow recovery.
- Prove preview-first behavior and explicit approval boundaries for SQL and workflows.
- Keep live LLM tests opt-in and secret-safe.
- Use E2E-managed Dockerized Ollama `qwen3:1.7b` for the mandatory live model gate.
- Start or reuse Ollama and pull `qwen3:1.7b` automatically before live LLM E2E runs.

## P1 Requirements

- Generate LLM E2E bridge tool definitions from production descriptors or a shared contract source.
- Add machine-readable vocabulary checks for protocol camelCase and ShardingSphere-owned snake_case.
- Prevent public legacy alias fields from returning.
- Add complex SQL safety examples and fail-safe tests.
- Split recovery or catalog builders only when they reduce reading cost or contract drift risk.
- Use typed builders or contract factories for recurring payload shapes when that improves clarity.

## P2 Requirements

- Add safe runtime diagnostic categories for missing driver, authentication failure, connection timeout, invalid configuration, unavailable database, and transport validation failure.
- Add packaged HTTP and STDIO diagnostic smoke tests where practical.
- Assert diagnostics and LLM artifacts do not leak JDBC credentials, bearer tokens, raw environment variables, or stack traces.
- Record final command evidence before moving the strict score to 100/100.
- Record the Ollama `qwen3:1.7b` live-gate command evidence before moving the strict score to 100/100.
- Record Docker/Ollama startup and model pull evidence before moving the strict score to 100/100.

## Final Score Rule

The strict score may become **100/100** only when all mandatory P0 and P1 gates pass.
P2 diagnostic readiness must have command evidence or a documented justified exception,
and the scorecard must record verification commands with exit codes.

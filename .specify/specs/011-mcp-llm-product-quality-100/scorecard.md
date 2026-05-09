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

# Scorecard: MCP LLM Product Quality 100

## Scope

This scorecard covers the full MCP product surface:

- Production modules: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features`, and `mcp/bootstrap`.
- End-to-end module: `test/e2e/mcp`.
- Runtime packaging and diagnostics where they affect MCP usability.

## Current Score

Strict current score: **78/100**.

This is the starting point for the new product-quality target. It does not overwrite the previous design-clarity score.

## Confirmed Gate Model

- Required live LLM model: Dockerized Ollama `qwen3:1.7b`.
- Required provider shape: OpenAI-compatible Ollama endpoint.
- Default endpoint: `http://127.0.0.1:11434/v1`.
- Default API key placeholder: `ollama`; no paid external API key is required.
- E2E responsibility: start or reuse Dockerized Ollama and pull `qwen3:1.7b` when absent.
- Multi-provider and multi-model runs are optional evidence.

## Weighted Dimensions

- LLM use friendliness and zero-guessing: **7/10**
- Interaction naturalness: **6/8**
- Semantic clarity: **6.5/8**
- Code readability: **8/10**
- Architecture clarity: **8/10**
- Decoupling: **6.5/8**
- Protocol contract completeness and drift protection: **8/10**
- Error recovery and diagnostics: **6.5/8**
- Safety and approval boundaries: **7/8**
- Test credibility for real LLM behavior: **7/10**
- Evolvability: **4/5**
- Operations and release readiness: **3.5/5**

Total: **78/100**.

## Target Score

Target score: **100/100**.

Every weighted dimension must reach full credit before the final score can be updated.

## Score Gaps

### P0 Gaps

- Natural LLM tasks are still too scripted in the current usability baseline.
- Live LLM proof is opt-in but not yet strong enough to justify a product-quality 100.
- Public model-facing contracts lack broad golden snapshot protection.
- Recovery payloads are structured but not yet proven as one-path self-healing across enough real model scenarios.
- Side-effect approval is present but needs stricter natural-task and negative-path evidence.

### P1 Gaps

- E2E bridge tool definitions still contain E2E-specific schema construction that can drift from production descriptors.
- Catalog and recovery builders are readable enough now, but their future growth needs stronger boundaries.
- Protocol camelCase and ShardingSphere snake_case coexist; the distinction needs machine-readable contract validation.
- SQL safety needs more complex examples and fail-safe evidence.

### P2 Gaps

- Packaged distribution diagnostics are not yet product-grade.
- Operator-facing failure categories are not yet covered across driver, auth, timeout, configuration, and transport validation paths.
- Secret leakage checks should be explicit in runtime diagnostics and LLM artifacts.

## Full-Credit Gates

- Natural LLM scenario gate passes without scripted first-call hints.
- The natural LLM scenario gate uses E2E-managed Dockerized Ollama `qwen3:1.7b`.
- Core LLM scenarios are blocking and must reach the full-score gate.
- Extended LLM scenarios are non-blocking only for model-performance outcomes.
- Extended LLM scenarios still hard-fail deterministic infrastructure, contract, safety, artifact, score-shape, and secret checks.
- Golden contract tests protect every public model-facing payload family.
- Recovery scenarios show successful one-error self-healing without extra invalid calls.
- Side-effect SQL and workflow tests prove preview-first and approval boundaries.
- Runtime diagnostics are safe, categorized, and tested.
- Packaged HTTP diagnostics are mandatory; packaged STDIO diagnostics are covered where practical.
- Code hotspots do not grow as mixed-responsibility sinks.
- Final verification commands are recorded with exit codes.

## Extended Scenario Scoring Rule

Extended scenarios write scorecards but do not move the final score by themselves.

Hard assertions:

- Ollama container starts or reuses successfully.
- `qwen3:1.7b` is pulled or already present.
- MCP runtime starts and closes cleanly.
- Scenario definitions are valid.
- Scorecards and artifacts are written.
- Scores are numeric and inside `0..100`.
- Interaction traces use known action and failure categories.
- Safety blocks unapproved side effects.
- No JDBC password, bearer token, raw environment value, or stack trace appears in artifacts.
- Golden model-facing contracts do not drift unexpectedly.

Scored, non-blocking assertions:

- Task success.
- First correct or optimal action.
- Extra invalid or recoverable calls.
- Round-trip count.
- Resource hit.
- Recovery success.
- Next-action following.
- Final-answer fidelity.

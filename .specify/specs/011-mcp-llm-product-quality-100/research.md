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

# Research: MCP LLM Product Quality 100

## Current Findings

- The previous design-clarity cleanup reached its intended target, but that score measured maintainability rather than real model usability.
- The current strict score is **78/100** because model behavior under natural prompts, contract drift protection, and operator diagnostics are not yet strong enough.
- The MCP surface already has useful primitives: capabilities, descriptors, prompts, completions, recovery payloads, next actions, preview modes, and opt-in LLM E2E.
- The remaining risk is not the lack of primitives. The remaining risk is whether models can consistently choose, recover, and stop safely without being over-scripted.

## Decisions

### Decision 1 - Use a New Speckit Package

Create `.specify/specs/011-mcp-llm-product-quality-100` instead of changing the previous design-clarity scorecard.

Rationale:

- The old score is valid for code clarity.
- The new score is stricter and product-facing.
- Mixing both scores would hide why the new baseline is lower.

### Decision 2 - Keep Live LLM Tests Opt-In

Default CI should not require an external paid provider.
The full 100-point gate should be reproducible through Dockerized Ollama.

Rationale:

- Provider availability, rate limits, and credentials are external.
- The suite should still be runnable locally and in controlled CI with explicit configuration.

### Decision 3 - Prefer Natural Tasks Over Scripted Tool Calls

The primary usability gate should not say "First call read_resource".

Rationale:

- Scripted first-call tasks prove protocol compliance, not model naturalness.
- Natural tasks better reveal whether capability discovery and descriptors are actually helpful.

### Decision 4 - Add Golden Contract Gates

Public payloads need snapshot or schema contract tests.

Rationale:

- MCP clients and models depend on stable field shape.
- Contract drift can pass behavior tests while still hurting model reliability.

### Decision 5 - Extract Boundaries Only When They Protect Score

Do not split classes just to make files smaller.

Rationale:

- The user values clarity over abstraction.
- New boundaries must reduce reading cost, reduce drift, or make tests stricter.

### Decision 6 - Extended Scenarios Are Scored, Not Loose

Extended LLM scenarios do not block the build for model-performance misses, but all deterministic properties still fail hard.

Rationale:

- Model choice, first action, final answer fidelity, and recovery success can vary with model capability.
- Artifact validity, scorecard shape, score bounds, safety blocking, contract shape, runtime setup, and secret redaction are deterministic.
- A non-blocking extended suite should reveal model weakness without hiding test harness, safety, or contract regressions.

## Resolved Questions

- The mandatory live LLM gate uses Dockerized Ollama with Alibaba Qwen model id `qwen3:1.7b`.
- All live LLM E2E suites use this model for the 100-point gate.
- MCP E2E owns the Dockerized Ollama lifecycle.
- MCP E2E automatically pulls `qwen3:1.7b` when the model is absent.
- The mandatory live gate uses the local Ollama OpenAI-compatible endpoint and does not require paid API keys.
- Multi-provider evidence is optional and not a hard gate.
- Natural task scenarios may be rewritten completely.
- Backward compatibility is not required because the MCP contract is unreleased.
- Golden contract protection should use JSON snapshots plus focused schema/assertion helpers.
- Packaged HTTP diagnostics are mandatory.
- Packaged STDIO diagnostics are covered where practical without turning the suite into a heavy framework.
- `preview` is always allowed.
- Real `execute_update` or `apply_workflow` execution requires an explicit approval signal.
- LLM usability scenarios default to preview, manual-only, and approval-violation checks rather than real side effects.
- Extended scenarios record model-performance scores without blocking, but deterministic assertions still fail hard.

## Open Questions

- Should the Ollama lifecycle be implemented with Testcontainers, Docker Compose, or existing E2E container support?
- Which packaged runtime failure cases are practical to simulate without slowing default E2E too much?

## Recommended Defaults

- Use MCP E2E-managed Dockerized Ollama `qwen3:1.7b` as the required live LLM gate and make multi-provider gates optional.
- Prefer Testcontainers if it fits the existing E2E style; otherwise use the repository's existing Docker E2E support.
- Require 100% pass on the minimal natural scenario set before claiming 100/100.
- Store golden snapshots under `test/e2e/mcp/src/test/resources` or module-local test resources, not under generated `target/`.
- Keep failure diagnostics secret-free and assert absence of known sensitive field names.
- Treat extended scenario task success, first-action optimality, answer fidelity, round-trip count, and recovery success as scored model-performance metrics.
- Treat extended scenario artifacts, score range, trace schema, safety blocking, contract snapshots, secret redaction, and runtime setup as hard assertions.

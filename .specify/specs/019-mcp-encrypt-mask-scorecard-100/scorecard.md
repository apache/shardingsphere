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

# Scorecard: MCP Encrypt/Mask Scorecard 100

## Rule

Every dimension has target score **100/100**. A dimension remains below 100 until closing evidence is present. Average score is not a completion gate.

## Current Baseline

- Assessment date: 2026-05-15.
- Overall baseline: **84/100**.
- `shardingsphere-mcp` baseline: **86/100**.
- `test/e2e/mcp` baseline: **80/100**.
- Functional completeness scope: encrypt and mask workflows only.
- Markdown rule: Markdown is not required for MCP tool results; structured JSON plus serialized JSON text fallback is the required tool-result contract.

## Active Dimensions

- **MCP protocol conformity**: current `100/100`, target `100/100`.
  Closed: source-map, descriptor validator, bootstrap transport, official discovery, structured content, output schema, and optional-capability evidence are current.
- **Encrypt/mask functional completeness**: current `100/100`, target `100/100`.
  Closed: encrypt and mask resources, prompts, completions, plan/apply/validate, approval, validation layers, drop/cleanup boundaries, and recovery paths have focused evidence.
- **Workflow safety and correctness**: current `100/100`, target `100/100`.
  Closed: preview-before-apply, explicit approval, session isolation, secret-safe elicitation, no data migration, and validation-layer boundaries have focused and E2E evidence.
- **AI usability and MCP ergonomics**: current `100/100`, target `100/100`.
  Closed: mcp-builder evaluation artifact now contains ten complex read-only questions; full LLM usability core and extended scorecards are `100.0` with `invalidCallRate=0.0`.
- **Architecture cleanliness**: current `100/100`, target `100/100`.
  Closed: custom validators/scanners, protocol/domain coupling, workflow payload field names, and bootstrap responsibilities are bounded in `architecture-cleanliness-evidence.md`.
- **Implementation elegance**: current `100/100`, target `100/100`.
  Closed: workflow field-name reuse, descriptor-driven behavior, guard clauses, and targeted boundary fixes are validated without broad rewrites.
- **Code cleanliness**: current `100/100`, target `100/100`.
  Closed: nullable-return review, mock-scope decisions, style checks, and historical score reconciliation are recorded and current.
- **Unit test coverage and quality**: current `100/100`, target `100/100`.
  Closed: public-method and branch-focused tests for encrypt/mask, core workflow, descriptors, recovery payloads, and input/output schema enforcement pass.
- **E2E and contract coverage**: current `100/100`, target `100/100`.
  Closed: default H2/HTTP, MySQL, STDIO, packaged distribution, URI boundaries, prompt/completion, recovery, and LLM usability lanes pass.
- **Security and risk control**: current `100/100`, target `100/100`.
  Closed: origin policy, OAuth/static authorization, no token passthrough, approval gates, SQL safety, redaction, and fail-closed behavior are covered.
- **Documentation and governance**: current `100/100`, target `100/100`.
  Closed: README, README_ZH, Speckit source map, scorecard, task ledger, and historical package notes are aligned.
- **Operations and distribution maturity**: current `100/100`, target `100/100`.
  Closed: distribution packaging, server metadata, startup diagnostics, configuration migration, performance budgets, and opt-in production lanes have evidence.

## Completion Status

- Dimensions at 100: **12/12** for this current package.
- This package is closed only for the declared scope: MCP encrypt and mask workflows plus their protocol, safety, usability, E2E, and distribution evidence.
- Historical packages `012`, `016`, `017`, and `018` remain reusable evidence sources, not automatic closure.
- Phase 2 protocol evidence tasks `T010` through `T013` are complete for source and focused bootstrap evidence.
- Phase 3 branch mapping tasks `T020` and `T021` are complete for encrypt/mask workflow branch evidence.
- Phase 3 product-path tasks `T022` and `T023` are complete for focused feature, core, and bootstrap evidence.
- Phase 4 safety tasks `T030` through `T034` are complete for focused unit, programmatic HTTP E2E, Checkstyle, and Spotless evidence.
- Phase 5 architecture and cleanliness tasks `T040` through `T044` are complete for bounded nullable returns, mock-scope decisions,
  workflow field names, input-schema/SQL scanner boundaries, and historical score reconciliation.
- Phase 6 E2E, LLM evaluation, and operations tasks `T050` through `T054` are complete with default, opt-in, distribution, LLM smoke, and full LLM usability evidence.
- Phase 7 final verification tasks `T060` through `T065` are complete with scoped unit, E2E, Checkstyle, Spotless, branch/status, and score closure evidence.

## Performance Budgets

- MCP unit reactor for touched modules: budget `<= 60s`, actual `35.860s`.
- Default `test/e2e/mcp` H2/HTTP lane: budget `<= 4m`, actual `3:00`.
- MySQL plus STDIO opt-in lane: budget `<= 3m`, actual `2:18`.
- Packaged distribution smoke: budget `<= 30s`, actual `6.098s`.
- mcp-builder artifact validator: budget `<= 15s`, actual `8.562s`.
- LLM smoke lane: budget `<= 15m`, actual `12:10`.
- Full LLM usability lane: budget `<= 25m`, actual `14:52`.
- Checkstyle/Spotless scoped style gates: budget `<= 15s` each, actual broad Checkstyle `8.963s`, broad Spotless `2.520s`.

## Final Evidence Snapshot

- `workflow-branch-coverage.md`: encrypt and mask branch-to-test mapping.
- `product-path-evidence.md`: feature, core, and bootstrap product-path tests.
- `architecture-cleanliness-evidence.md`: code cleanliness, abstraction, and boundary review.
- `e2e-llm-operations-evidence.md`: default E2E, opt-in runtime, distribution, LLM, style, and branch/status evidence.
- Latest full LLM usability artifacts: `test/e2e/mcp/target/llm-e2e/20260516015738-b8a8d5be/llm-usability-h2/core/summary.md`
  and `test/e2e/mcp/target/llm-e2e/20260516015738-b8a8d5be/llm-usability-h2/extended/summary.md`.

## Evidence Policy

- Valid evidence: scoped Maven command with exit code, Checkstyle/Spotless result, Jacoco report where needed, E2E artifact, LLM evaluation artifact,
  official-source mapping, or documented SDK limitation with local source evidence.
- Invalid evidence: prose-only rationale, average score, unverified historical score, generated `target/` content, or optional capability claims without protocol-visible proof.

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

- **MCP protocol conformity**: current `88/100`, target `100/100`.
  Gap: refresh official MCP and SDK source evidence for lifecycle, transport, authorization, discovery, structured content, output schema, and optional capabilities.
- **Encrypt/mask functional completeness**: current `87/100`, target `100/100`.
  Gap: prove resources, prompts, completions, plan/apply/validate, approval, validation layers, drop/cleanup boundaries, and recovery paths.
- **Workflow safety and correctness**: current `88/100`, target `100/100`.
  Gap: prove preview-before-apply, approval gate, session isolation, secret-safe elicitation, no data migration, and complete validation layers.
- **AI usability and MCP ergonomics**: current `90/100`, target `100/100`.
  Gap: replace shallow evaluation evidence with ten complex read-only mcp-builder questions and prove navigation, next actions, recovery, and completion.
- **Architecture cleanliness**: current `84/100`, target `100/100`.
  Gap: reduce or explicitly bound custom validators/scanners, protocol/domain coupling, stringly typed workflow payloads, and bootstrap responsibility creep.
- **Implementation elegance**: current `83/100`, target `100/100`.
  Gap: improve type safety and reusable workflow contracts while keeping descriptor-driven behavior and guard clauses clear.
- **Code cleanliness**: current `78/100`, target `100/100`.
  Gap: remove or justify nullable production returns, direct static/constructor mocking in touched tests, duplicated historical claims, and style deviations.
- **Unit test coverage and quality**: current `85/100`, target `100/100`.
  Gap: add or revalidate public-method tests, branch coverage, descriptor validators, recovery payloads, and input/output schema enforcement.
- **E2E and contract coverage**: current `81/100`, target `100/100`.
  Gap: make H2, MySQL, STDIO, distribution, packaged runtime, URI boundaries, completion positives, OAuth/security, and session cleanup current.
- **Security and risk control**: current `87/100`, target `100/100`.
  Gap: prove origin policy, OAuth/static authorization, no token passthrough, approval gates, SQL safety, redaction, and fail-closed behavior.
- **Documentation and governance**: current `84/100`, target `100/100`.
  Gap: align README, README_ZH, Speckit, source maps, scorecards, evidence ledgers, and historical package notes.
- **Operations and distribution maturity**: current `72/100`, target `100/100`.
  Gap: prove distribution packaging, server metadata, startup diagnostics, configuration migration, performance budgets, and opt-in production lanes.

## Completion Status

- Dimensions at 100: **0/12** for this current package.
- This package intentionally resets the active current checkpoint below historical 100/100 claims until fresh evidence is attached.
- Historical packages `012`, `016`, `017`, and `018` remain reusable evidence sources, not automatic closure.

## Evidence Policy

- Valid evidence: scoped Maven command with exit code, Checkstyle/Spotless result, Jacoco report where needed, E2E artifact, LLM evaluation artifact,
  official-source mapping, or documented SDK limitation with local source evidence.
- Invalid evidence: prose-only rationale, average score, unverified historical score, generated `target/` content, or optional capability claims without protocol-visible proof.

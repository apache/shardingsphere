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

# Scorecard: MCP Encrypt/Mask Scoped Scorecard 100

## Scoring Rule

Every dimension targets **100/100**. A dimension can reach 100 only when all scoped gaps are closed with evidence. Non-goals do not reduce the score.

## Current Baseline

- Assessment date: 2026-05-16.
- Overall scoped baseline: **88/100**.
- Protocol scope: MCP `2025-11-25` only.
- SDK scope: MCP Java SDK `1.1.2`, fixed.
- Functional scope: encrypt and mask workflows only.
- Elegance rule: readability and clear structure outrank broad abstraction.

## Explicit Non-Goals

- MCP `icons` and `Tool.execution`.
- SDK upgrade or dependency version change.
- Protocol compatibility proof for revisions other than `2025-11-25`.
- Sharding, readwrite-splitting, shadow, traffic governance, mode governance, observability, and general administration.
- Data migration, backfill, rollback orchestration, and persistent audit storage.

## Active Dimensions

| Dimension | Current | Target | Closing evidence needed |
| --- | ---: | ---: | --- |
| MCP protocol conformity | 95 | 100 | Contract tests for declared `2025-11-25` methods, SDK `1.1.2` scope documentation, structured content/output schema checks, transport/session negative cases. |
| Encrypt/mask functional completeness | 91 | 100 | End-to-end lifecycle evidence for resources, prompts, completions, plan, preview, approval apply, validation, and recovery for encrypt and mask. |
| Implementation elegance | 88 | 100 | Minimal readability-first cleanup of duplicated workflow payload construction, no broad framework rewrite, clear handler/service boundaries. |
| AI usability and MCP ergonomics | 91 | 100 | Ten mcp-builder evaluation questions, stable `next_actions`, useful resource links, prompt coverage for common encrypt/mask operator intents. |
| Safety and approval control | 90 | 100 | Negative tests for approval bypass, session isolation, redaction, token/origin failures, unsafe SQL or malformed workflow inputs. |
| Architecture cleanliness | 89 | 100 | Dependency boundary review, feature isolation, descriptor validation ownership, lifecycle clarity for static registries where they affect testability. |
| Code cleanliness | 83 | 100 | Remove or justify direct private reflection, direct static/constructor mocks, broad `containsString` assertions, and unexplained Checkstyle suppressions. |
| Test coverage and quality | 84 | 100 | Public-method test map, branch coverage matrix, Jacoco evidence where relevant, default and focused test commands with exit codes. |
| Documentation and operations handoff | 87 | 100 | README/Speckit/validator alignment for scoped non-goals, encrypt/mask quickstart, troubleshooting, and evidence ledger. |
| Performance and reliability evidence | 84 | 100 | Budgets and tests for descriptor loading, workflow planning, metadata/resource operations, E2E duration, distribution smoke, and session cleanup. |

## Evidence Policy

Valid evidence:

- Scoped Maven command with exit code.
- Checkstyle, Spotless, or Jacoco report.
- E2E or LLM evaluation artifact.
- Source map to official MCP `2025-11-25` documentation or local SDK `1.1.2` behavior.
- Explicit non-goal decision captured in `spec.md`, `scorecard.md`, and README.

Invalid evidence:

- Historical 100/100 claims without current revalidation.
- Average score alone.
- Generated `target/` content as a source edit.
- Claims for optional MCP capabilities that are not declared, implemented, or intentionally excluded.

## Score Closure Rule

- A task may be marked complete only after its evidence is recorded in `tasks.md` or a linked evidence file.
- A dimension may be moved to 100 only after every task mapped to that dimension is complete.
- Final closure requires `git branch --show-current` to remain `001-shardingsphere-mcp`.

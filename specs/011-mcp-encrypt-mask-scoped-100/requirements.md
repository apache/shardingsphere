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

# Requirements: MCP Encrypt/Mask Scoped Scorecard 100

## Canonical Speckit Package

This is the repo-visible handoff for `.specify/specs/020-mcp-encrypt-mask-scoped-100/`.

Canonical files:

- `.specify/specs/020-mcp-encrypt-mask-scoped-100/spec.md`
- `.specify/specs/020-mcp-encrypt-mask-scoped-100/plan.md`
- `.specify/specs/020-mcp-encrypt-mask-scoped-100/source-map.md`
- `.specify/specs/020-mcp-encrypt-mask-scoped-100/scorecard.md`
- `.specify/specs/020-mcp-encrypt-mask-scoped-100/tasks.md`
- `.specify/specs/020-mcp-encrypt-mask-scoped-100/checklists/requirements.md`

## Goal

Raise every scoped MCP encrypt/mask score dimension to **100/100** without switching branches.

## User Constraints

- Do not switch branches.
- Do not upgrade or change MCP Java SDK `1.1.2`.
- Do not implement MCP icons or `Tool.execution`.
- Only cover MCP protocol revision `2025-11-25`.
- Only score encrypt and mask functional completeness.
- Prefer readable, elegant, minimal code over broad abstraction.
- Use Docker/Testcontainers for local opt-in lanes when Docker, network, and local resources are available.
- LLM score evidence must use Docker-owned Ollama with `qwen3:1.7b`; external LLM endpoints are debug-only.
- Use Codex CLI for cross-model second opinion after confirming the exact read-only command.

## Active Scores

- MCP protocol conformity: `95 -> 100`
- Encrypt/mask functional completeness: `91 -> 100`
- Implementation elegance: `88 -> 100`
- AI usability and MCP ergonomics: `91 -> 100`
- Safety and approval control: `90 -> 100`
- Architecture cleanliness: `89 -> 100`
- Code cleanliness: `83 -> 100`
- Test coverage and quality: `84 -> 100`
- Documentation and operations handoff: `87 -> 100`
- Performance and reliability evidence: `84 -> 100`

## Implementation Order

1. Lock scope, score baseline, and non-goals.
2. Close MCP `2025-11-25` protocol evidence under SDK `1.1.2`.
3. Complete encrypt/mask workflow lifecycle coverage.
4. Harden AI usability, safety, code quality, and public-API tests.
5. Add documentation, operations, performance, and E2E evidence.
6. Update scores to 100 only after verification passes and branch remains unchanged.

## Verification Lanes

- Default lane: branch/status, Spec Kit checks, scoped MCP unit tests, Checkstyle, Spotless, Jacoco for touched production classes, and H2/HTTP MCP E2E.
- Opt-in lane: Docker/Testcontainers-backed MySQL, Proxy encrypt/mask workflow, Docker image STDIO, packaged distribution, and Docker-owned Ollama LLM evaluation.

## Completion Rule

A score dimension is not complete until the corresponding tasks in `.specify/specs/020-mcp-encrypt-mask-scoped-100/tasks.md` have passing evidence.

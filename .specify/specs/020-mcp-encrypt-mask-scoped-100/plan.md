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

# Implementation Plan: MCP Encrypt/Mask Scoped Scorecard 100

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-16 | **Spec**: `.specify/specs/020-mcp-encrypt-mask-scoped-100/spec.md`
**Input**: Feature specification from `.specify/specs/020-mcp-encrypt-mask-scoped-100/spec.md`
**Note**: This package is manually maintained because branch-changing Speckit commands are forbidden.

## Summary

Raise the ten scoped MCP encrypt/mask score dimensions from the current `88/100` reassessment to `100/100`.
The plan intentionally excludes MCP icons, SDK upgrades, non-`2025-11-25` compatibility proof, and non-encrypt/mask ShardingSphere features.

## Technical Context

**Language/Version**: Java 21 for the MCP subchain.
**Primary Dependencies**: MCP Java SDK `1.1.2`, embedded Tomcat, ShardingSphere-Proxy runtime, JUnit 5, Mockito.
**Storage**: No new persistent storage required by this score package.
**Protocol Target**: MCP `2025-11-25` only.
**Testing**: Focused Maven unit tests, default MCP E2E, opt-in Proxy/MySQL/STDIO/distribution/LLM lanes, Checkstyle, Spotless, Jacoco where coverage is asserted.
**Target Runtime**: ShardingSphere MCP over Streamable HTTP and STDIO, focused on encrypt/mask workflows through ShardingSphere-Proxy.
**Constraints**: No branch switching, no SDK upgrade, no package-management changes, no generated `target/` edits, no broad abstractions for their own sake.

## Constitution Check

- **Proxy-first logical abstraction**: Pass. Encrypt/mask workflows target ShardingSphere-Proxy logical database behavior.
- **Explicit operator control**: Pass. Preview and approval remain mandatory before side effects.
- **Minimal safe automation**: Pass. Data migration, backfill, rollback orchestration, and persistent audit storage are out of scope.
- **Deterministic naming and transparent changes**: Pass. Encrypt derived names and mask rule changes require visible plan and validation evidence.
- **Complete verification before completion**: Pass only after every score dimension has linked evidence.
- **Repository rules**: Pass only after `CODE_OF_CONDUCT.md` style, test, mocking, reflection, and build requirements are verified for touched modules.

## Project Structure

### Speckit Package

```text
.specify/specs/020-mcp-encrypt-mask-scoped-100/
|-- checklists/
|   `-- requirements.md
|-- plan.md
|-- scorecard.md
|-- source-map.md
|-- spec.md
`-- tasks.md

specs/011-mcp-encrypt-mask-scoped-100/
`-- requirements.md
```

### Source Paths

```text
mcp/support/
mcp/core/
mcp/features/encrypt/
mcp/features/mask/
mcp/bootstrap/
mcp/README.md
mcp/README_ZH.md
distribution/mcp/
test/e2e/mcp/
```

## Execution Strategy

1. Lock the scoped score contract and non-goals before implementation.
2. Close protocol evidence without icons, SDK upgrades, or multi-version compatibility work.
3. Close encrypt/mask workflow lifecycle gaps through focused tests and E2E evidence.
4. Clean code and tests only where they affect score closure, readability, or repository compliance.
5. Record documentation, operations, and performance evidence after behavior is proven.
6. Move scores to 100 only after final verification passes on the current branch.

## Risk Controls

- Do not use `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Do not mark a score 100 from prose-only rationale.
- Do not expand feature scope beyond encrypt/mask.
- Do not upgrade MCP Java SDK or alter dependency versions.
- Do not introduce framework-style abstractions unless they remove current duplication and improve readability.

## Verification Bar

Required before final score closure:

- Scoped unit tests for touched MCP modules.
- Focused tests for protocol, descriptor, workflow, safety, and recovery changes.
- Public methods and reachable branches in touched production classes are covered through public APIs and varied inputs.
- No private method is invoked by reflection, and no production method is made public only for tests.
- Default MCP E2E lane.
- Checkstyle and Spotless for touched modules.
- Jacoco when a coverage score depends on branch or method coverage.
- Distribution and opt-in runtime lanes recorded separately when infrastructure is available.
- Final branch check showing `001-shardingsphere-mcp`.

## Confirmed Implementation Decisions

- Start with Phase 1, Phase 2, and Phase 7 tasks: scope baseline, protocol evidence, and code/test cleanliness.
- Modify README, README_ZH, docs, and Speckit files as needed for scope and evidence alignment.
- Review direct static and constructor mocking case by case; migrate only when readability or leak risk warrants it.
- Use public APIs and varied inputs to reach public-method and branch coverage for touched production classes.
- Do not reflectively test private methods, and do not widen production visibility only for tests.
- Treat opt-in lanes as environment-dependent verification paths, not default-lane blockers.
- Run a fresh-context doubt-driven review before the next implementation round.
- Use `codex exec --ephemeral --sandbox read-only -C /Users/zhangliang/IdeaProjects/shardingsphere - < /tmp/mcp-doubt-review.md`
  for the cross-model second opinion when the prompt file is current.

## Lane Definitions

- **Default lane**: branch/status checks, Spec Kit/document checks, scoped MCP unit tests, Checkstyle, Spotless, Jacoco for touched production classes,
  and default H2/HTTP MCP E2E that does not require external services or credentials.
- **Opt-in lane**: MySQL E2E, Proxy encrypt/mask workflow E2E, Docker image STDIO, packaged distribution smoke, and LLM evaluation.
  These lanes are local when Docker/Testcontainers can start required containers and pull required images/models.
- **LLM opt-in note**: score-closing LLM evidence must use Docker-owned `ollama/ollama:0.23.1` with `qwen3:1.7b`.
  The support layer must not silently reuse an external OpenAI-compatible endpoint for score evidence.
  External endpoints may remain available only as an explicit debug mode outside score closure.
  Evidence should record the resolved image digest for reproducibility.

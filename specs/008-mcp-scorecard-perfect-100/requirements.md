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

# MCP Scorecard Perfect 100 Requirements

## Source

This is the repo-visible handoff for `.specify/specs/012-mcp-scorecard-perfect-100/`.

Canonical Speckit files:

- `.specify/specs/012-mcp-scorecard-perfect-100/spec.md`
- `.specify/specs/012-mcp-scorecard-perfect-100/plan.md`
- `.specify/specs/012-mcp-scorecard-perfect-100/scorecard.md`
- `.specify/specs/012-mcp-scorecard-perfect-100/source-driven-mcp-standard-map.md`
- `.specify/specs/012-mcp-scorecard-perfect-100/tasks.md`
- `.specify/specs/012-mcp-scorecard-perfect-100/checklists/requirements.md`

## Goal

Every MCP production and MCP E2E score dimension must reach **100/100**.
The feature is not complete if only the average reaches 100.

2026-05-13 update:

- All MCP implementation behavior must follow official MCP standards.
- ShardingSphere-specific fields may be application payload only; they cannot replace official MCP discovery, transport, authorization, pagination, or error semantics.
- Source-driven-development evidence is required before changing protocol, SDK, security, or E2E behavior.
- The earlier 2026-05-11 perfect-score checkpoint is historical; the 2026-05-13 scoped official-standard gate is closed by current evidence.

2026-05-14 update:

- The user expanded HTTP authorization from scoped deployment-level bearer-token matching to complete OAuth token validation.
- The complete OAuth gate is reopened until MCP HTTP bearer tokens are validated as OAuth resource-server tokens with active, issuer, audience/resource, time-window, and scope checks.
- The implementation direction is introspection-first through official MCP Authorization, RFC 6750, RFC 7662, RFC 8707, and RFC 8414.
- The previous 2026-05-13 12/12 standard-first result remains historical scoped evidence, but it cannot close the new complete OAuth validation gate.

Latest baseline:

- MCP production modules: **87.5/100**.
- MCP E2E module: **86.3/100**.
- Equal-weight combined checkpoint: **86.9/100**.

## Scope

- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/features/encrypt`
- `mcp/features/mask`
- `mcp/bootstrap`
- `distribution/mcp`
- `test/e2e/mcp`

## Constraints

- Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Do not mark any dimension 100 without evidence.
- Do not use average-score semantics as final completion.
- Do not change the project's existing PR gate.
- Treat the listed dimensions as locked for this checkpoint unless the user explicitly changes them.
- Keep historical Speckit package `011-mcp-llm-product-quality-100` as historical context only.
- Use MCP Specification `2025-11-25` as the protocol source of truth.
- Verify MCP Java SDK usage against detected SDK `1.1.2` or local dependency source.
- Do not treat `shardingsphere://capabilities` or similar resources as official MCP protocol discovery.
- Do not mark standard-first dimensions `100/100` while official MCP evidence is missing.
  The 2026-05-13 scoped standard-first dimensions were marked `100/100` because `EV-026` through `EV-032` were current.
- Do not mark the 2026-05-14 complete OAuth validation gate complete until OAuth introspection, resource/audience, issuer, time-window, scope, 401/403 challenge,
  fail-closed, and no-token-passthrough evidence exists.

## Required Score Model

Production dimensions must each reach 100:

- Model-use friendliness
- Natural interaction quality
- Clarity
- Code readability
- Architecture clarity
- Decoupling
- Protocol correctness
- Stability
- Diagnostics
- Safety
- Extensibility
- Performance and resource use
- Configuration and distribution
- Compatibility
- Test quality

E2E dimensions must each reach 100:

- Model-use friendliness
- Natural interaction quality
- Clarity
- Code readability
- Architecture clarity
- Decoupling
- Protocol correctness
- End-to-end realism
- Stability
- Diagnostics
- Safety
- Extensibility
- Performance and resource use
- Configuration and distribution
- Compatibility
- Test quality

Active standard-first dimensions must also each reach 100:

- MCP protocol standard conformity
- Tool and resource model quality
- Functional completeness
- Implementation elegance
- Architecture cleanliness
- Code cleanliness
- Security and risk control
- Error recovery and diagnostics
- Test coverage
- E2E realism and LLM evaluation
- Operations and distribution maturity
- Documentation and governance

Active complete OAuth validation dimensions currently reopened:

- MCP protocol standard conformity: `90/100`
- Functional completeness: `88/100`
- Security and risk control: `70/100`
- Test coverage: `85/100`
- Operations and distribution maturity: `85/100`
- Documentation and governance: `85/100`

## Completion Rule

Completion requires:

- Every dimension target is `100/100`.
- Every below-100 baseline gap has a closing task.
- Every claimed 100 dimension has command, artifact, or contract evidence.
- Relevant scoped Maven tests, Checkstyle, and Spotless gates pass for touched Java modules.
- The current git branch remains unchanged.
- Official MCP source mapping is current.
- All protocol behavior is backed by MCP Specification or verified SDK `1.1.2` evidence.
- mcp-builder evaluation XML exists with ten read-only, independent, complex, realistic, verifiable, and stable questions.
- Complete OAuth token validation is implemented and verified when HTTP authorization is enabled.

## Current Implementation Evidence

Default-lane and opt-in runtime evidence have been refreshed for this checkpoint:

- Final tests command:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipITs -Dspotless.skip=true clean test -B -ntp
```

- Result: exit `0`, all seven selected modules `SUCCESS`, total `03:22 min`.
- Counts: `mcp/api` `13`, `mcp/support` `278`, `mcp/core` `418`, `mcp/features/encrypt` `69`, `mcp/features/mask` `52`, `mcp/bootstrap` `170`.
- E2E counts: `test/e2e/mcp` `240` with `14` skipped.
- Checkstyle and Spotless both pass for `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, `mcp/bootstrap`, and `test/e2e/mcp`.

Additional opt-in runtime evidence:

- STDIO runtime: exit `0`, `84` tests, `0` skipped, total `08:25 min`.
- MySQL HTTP plus STDIO runtime: exit `0`, `22` tests, `0` skipped, total `02:55 min`, Docker-backed `mysql:8.0.36`.
- Distribution package: exit `0`, all `50` reactor modules `SUCCESS`, total `15.463 s`.
- Packaged HTTP, STDIO, and plugin smoke: exit `0`, `3` tests, `0` skipped, total `6.124 s`.
- Live LLM smoke plus usability: exit `0`, `5` tests, `0` skipped, total `32:12 min`, run ID `ra001-final-20260512015143`.
- Live LLM scorecards: core and extended suites both `100/100`, with `invalidCallRate=0`, `approvalViolationRate=0`, and `harnessRecoveryRate=0`.

Current standard-first closure evidence:

- Protocol evidence against official MCP sources: closed by `EV-026` and `EV-027`.
- Streamable HTTP security and authorization behavior: closed by `EV-028`.
- Official resource template, prompt, completion, and golden-contract coverage: closed by `EV-029`.
- mcp-builder XML evaluation evidence: closed by `EV-030`.
- Scoped tests and style gates: closed by `EV-031` and `EV-032`.
- Current branch remains `001-shardingsphere-mcp`; branch-changing Speckit scripts remain forbidden.

Current complete OAuth validation status:

- Reopened on 2026-05-14.
- Phase 6 tasks `T080` through `T088` are open.
- Active complete OAuth validation dimensions at 100: `0/6`.
- Overall status is reopened until implementation and verification evidence is recorded.

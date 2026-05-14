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

# Quickstart: MCP Scorecard Perfect 100

## Read Order

1. `spec.md`
2. `scorecard.md`
3. `evidence-ledger.md`
4. `source-driven-mcp-standard-map.md`
5. `protocol-evidence-matrix.md`
6. `reanalysis.md`
7. `tasks.md`
8. `plan.md`
9. `research.md`
10. `data-model.md`
11. `checklists/requirements.md`

## Current Gate

This package is now both the requirement artifact and the current evidence ledger for the scoped standard-first implementation.
It claims `100/100` only for dimensions backed by the evidence records in `scorecard.md` and `evidence-ledger.md`.

Current baseline:

- MCP production modules: `87.5/100`.
- MCP E2E module: `86.3/100`.
- Required target: every dimension `100/100`.
- Active 2026-05-13 standard-first gate: closed by `EV-026` through `EV-032`.

## Local Validation

Run these read-only checks after editing this package:

```bash
git status --short --branch
find .specify/specs/012-mcp-scorecard-perfect-100 specs/008-mcp-scorecard-perfect-100 -type f | sort
rg -n "git switch|git checkout|branch-changing|100/100|Target" .specify/specs/012-mcp-scorecard-perfect-100 specs/008-mcp-scorecard-perfect-100
STANDARD_PATTERN="source-driven|official MCP|modelcontextprotocol|non-standard|protocol invention|cursor|nextCursor|structuredContent|outputSchema"
rg -n "$STANDARD_PATTERN" .specify/specs/012-mcp-scorecard-perfect-100 specs/008-mcp-scorecard-perfect-100
```

Expected result:

- The branch remains `001-shardingsphere-mcp`.
- The new Speckit package and repo-visible handoff files exist.
- Branch-changing commands appear only as forbidden constraints.
- Official MCP standard requirements are visible in the canonical package and repo-visible handoff.

## Future Implementation Verification

When code or tests change, use scoped commands such as:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipITs -Dspotless.skip=true clean test -B -ntp
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipTests -DskipITs -Dspotless.skip=true -Pcheck checkstyle:check -B -ntp
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipTests -DskipITs -Pcheck spotless:check -B -ntp
```

Opt-in evidence may also require MySQL, STDIO, distribution, and live LLM commands.
Those commands must be recorded in `scorecard.md` before the related dimensions can reach 100.

## E2E Evidence Lane Matrix

| Lane | Default | Current command or artifact | Latest result |
|------|---------|-----------------------------|---------------|
| H2 HTTP production runtime | Enabled | Default MCP plus E2E scoped test command | exit `0`, `test/e2e/mcp` `240` tests with `14` skipped |
| H2 STDIO production runtime | Opt-in | `-Dmcp.e2e.production.stdio.enabled=true` with `ProductionH2*` and `ProductionMultiDatabaseE2ETest` | exit `0`, `84` tests, `0` skipped |
| MySQL HTTP and STDIO runtime | Opt-in | MySQL plus STDIO flags with `ProductionMySQLRuntimeSmokeE2ETest` | exit `0`, `22` tests, `0` skipped |
| Packaged distribution HTTP and STDIO | Opt-in | Build `distribution/mcp`, then `PackagedDistributionSmokeE2ETest` | exit `0`, packaged HTTP and STDIO smoke passed |
| Packaged plugin discovery | Opt-in | `PackagedDistributionPluginDiscoveryE2ETest` | exit `0`, plugin discovery passed |
| Live LLM usability | Opt-in | `-Pllm-e2e -Dtest=LLMSmokeE2ETest,LLMUsabilitySuiteE2ETest` | exit `0`, `5` tests, core and extended scorecards `100/100` |

## Current Implementation Verification

This checkpoint now has current default-lane and opt-in runtime evidence:

- Final tests command:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipITs -Dspotless.skip=true clean test -B -ntp
```

- Result: exit `0`, all seven selected modules `SUCCESS`, total `03:22 min`.
- Counts: `mcp/api` `13`, `mcp/support` `278`, `mcp/core` `418`, `mcp/features/encrypt` `69`, `mcp/features/mask` `52`, `mcp/bootstrap` `170`.
- E2E counts: `test/e2e/mcp` `240` with `14` skipped.
- Checkstyle: exit `0`, `0 Checkstyle violations`.
- Spotless: exit `0`, all selected modules clean.

Opt-in runtime evidence:

- STDIO runtime command: exit `0`, `84` tests, total `08:25 min`.
- MySQL HTTP plus STDIO command: exit `0`, `22` tests, total `02:55 min`.
- Distribution package command: exit `0`, all `50` reactor modules `SUCCESS`, total `15.463 s`.
- Packaged HTTP, STDIO, and plugin smoke command: exit `0`, `3` tests, total `6.124 s`.
- Live LLM smoke plus usability command: exit `0`, `5` tests, total `32:12 min`, run ID `ra001-final-20260512015143`.
- Live LLM artifacts: core and extended scorecards both reported `overallScore=100`, `fullScore=true`,
  `invalidCallRate=0`, `approvalViolationRate=0`, and `harnessRecoveryRate=0`.

Final perfect-score slice:

- MCP API/support/core safety, protocol, and performance budget tests: exit `0`, `72` tests.
- H2 capability E2E smoke after safety-policy payload change: exit `0`, `7` tests.
- MCP API/support/core Checkstyle with dependencies: exit `0`, `0 Checkstyle violations`.
- MCP API/support/core Spotless with dependencies: exit `0`, all clean.

This evidence closes the 2026-05-11 performance-budget, safety-boundary, protocol-conformance,
historical revalidation, and decoupling gaps.

Standard-first slice:

- Protocol matrix and source map: current in `EV-026` and `EV-027`.
- HTTP authorization and protected-resource metadata: current in `EV-028`.
- Golden contract plus production H2 official-discovery E2E: exit `0`, `13` concrete tests across `HttpTransportGoldenContractE2ETest`,
  `ProductionH2CapabilityDiscoveryE2ETest`, and `ProductionH2AiNativeInteractionE2ETest`.
- mcp-builder evaluation artifact: `10` read-only Q/A pairs, verified by `MCPBuilderEvaluationArtifactTest`.
- Scoped standard-first tests: exit `0`, `81` tests across support, core, bootstrap, and MCP E2E.
- Scoped standard-first Checkstyle and Spotless: exit `0`, all four touched modules clean.

This closes the 2026-05-13 standard-first gate for the scoped modules and artifacts.

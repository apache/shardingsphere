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
- Original scoped baseline: **88/100**.
- Current scoped score: **90/100** after Phase 1 baseline evidence, Phase 2 MCP protocol closure, and Phase 3 encrypt/mask functional closure.
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
| MCP protocol conformity | 100 | 100 | Closed by contract tests for declared `2025-11-25` methods, SDK `1.1.2` scope documentation, structured content/output schema checks, and transport/session negative cases. |
| Encrypt/mask functional completeness | 100 | 100 | Closed by branch matrices plus unit and Proxy E2E evidence for resources, prompts, completions, plan, preview, approval apply, validation, and recovery for encrypt and mask. |
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
- E2E or LLM evaluation artifact. LLM score evidence must record Docker-owned `ollama/ollama:0.23.1` runtime usage.
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

## Evidence Ledger

### Phase 1: Scoped Baseline and Governance

- T001: `git branch --show-current` returned `001-shardingsphere-mcp` with exit code `0`.
- T002: `mcp/README.md` and `mcp/README_ZH.md` already state SDK `1.1.2`, MCP `2025-11-25`, icons/`Tool.execution` non-goals, and the narrowed public MCP surface.
- T003: historical `.specify/specs/019-mcp-encrypt-mask-scorecard-100/` files are retained as previous evidence only; they do not automatically close this scoped package.
- T004: this ledger records every current score dimension and its closing evidence state before further score movement.

### Phase 2: MCP Protocol Conformity

- T010: `MCPSyncServerFactoryTest` verifies declared resource, resource template, tool, prompt, and completion capabilities exposed through SDK `1.1.2`.
- T011: `StreamableHttpMCPServletTest`, `StreamableHttpMCPServerWireTest`, `ProtocolVersionHeaderConstraintTest`, and `ShardingSphereServerTransportSecurityValidatorTest`
  verify POST, GET, DELETE, session id, negotiated protocol header, missing protocol header, and unsupported HTTP content type behavior.
- T012: `MCPTransportPayloadUtilsTest` verifies schema-conforming `structuredContent` plus serialized JSON text fallback for tool result payloads.
- T013: `MCPDescriptorCatalogValidatorTest` and `MCPDescriptorYamlKeyValidatorTest` verify scoped non-goal fields are not required, unsupported `icons` and `Tool.execution`
  descriptor keys are rejected, and legacy public alias fields remain rejected.
- T014: `source-map.md` records that non-`2025-11-25` compatibility tests are not score-closing evidence for this package.
- Verification: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest=MCPSyncServerFactoryTest,StreamableHttpMCPServletTest,StreamableHttpMCPServerWireTest,MCPTransportPayloadUtilsTest,ProtocolVersionHeaderConstraintTest,ShardingSphereServerTransportSecurityValidatorTest,MCPDescriptorCatalogValidatorTest,MCPDescriptorYamlKeyValidatorTest test` exited `0`.
- Regression verification: `./mvnw -pl mcp/support,mcp/bootstrap -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`.
- Coverage verification: `./mvnw -pl mcp/support,mcp/bootstrap -DskipITs -Dspotless.skip=true -Djacoco.skip=false -Dsurefire.failIfNoSpecifiedTests=false test jacoco:report` exited `0`;
  `StreamableHttpMCPServlet` has JaCoCo `METHOD missed=0 covered=24`, `BRANCH missed=0 covered=18`, and `LINE missed=0 covered=66`.
- Style verification: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check` exited `0`.

### Phase 3: Encrypt/Mask Functional Completeness

- T020: `workflow-coverage.md` records the encrypt branch matrix for create, alter, drop non-goal cleanup, missing algorithm, missing properties,
  assisted query, LIKE query conflict, rule conflict, missing metadata, validation failure, and validation success.
- T021: `workflow-coverage.md` records the mask branch matrix for create, alter, drop, missing algorithm, missing field semantics,
  existing rule conflict, missing metadata, validation failure, recovery, and validation success.
- T022: existing encrypt descriptor, resource, completion, handler, planning, validation, and DistSQL tests are mapped in `workflow-coverage.md`.
- T023: existing mask descriptor, resource, completion, handler, planning, validation, and DistSQL tests are mapped in `workflow-coverage.md`.
- T024: encrypt preview, approval apply, validation, partial-apply failure, and recovery coverage is mapped through `WorkflowExecutionServiceTest`,
  `EncryptWorkflowValidationServiceTest`, and `HttpProductionProxyEncryptWorkflowE2ETest`.
- T025: mask preview, approval apply, validation, drop, and recovery coverage is mapped through `WorkflowExecutionServiceTest`,
  `MaskWorkflowValidationServiceTest`, and `HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy`.
- T026: Proxy-backed product-path E2E coverage exists for encrypt and mask workflows; approved-step recovery was verified through Docker/Testcontainers.
- Reliability closure: `MySQLRuntimeTestSupport` now waits for the MySQL `ready for connections` log on port `3306`, so Proxy-backed E2E starts only after JDBC can accept connections.
- Verification attempt: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `1` because dependency modules were not rebuilt with the selected module.
- Verification: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; after the MySQL readiness fix, it ran 1 test with 0 failures, 0 errors, and 0 skipped tests through local Docker/Testcontainers MySQL, embedded ShardingSphere-Proxy, and HTTP MCP.
- Verification attempt: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `1` because dependency modules were not rebuilt with the selected module.
- Verification: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; after the MySQL readiness fix, it ran 1 test with 0 failures, 0 errors, and 0 skipped tests through local Docker/Testcontainers MySQL, embedded ShardingSphere-Proxy, and HTTP MCP.
- Scoped unit verification: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`.
- Scoped style verification: `./mvnw -pl mcp/support,mcp/core,test/e2e/mcp -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check`
  exited `0`.

### Remaining Dimensions

- Implementation elegance: remains `88/100`; Phase 6 cleanup is not complete.
- AI usability and MCP ergonomics: remains `91/100`; Phase 4 tasks are not complete.
- Safety and approval control: remains `90/100`; Phase 5 tasks are not complete.
- Architecture cleanliness: remains `89/100`; Phase 6 tasks are not complete.
- Code cleanliness: remains `83/100`; Phase 7 tasks are not complete.
- Test coverage and quality: remains `84/100`; Phase 8 Jacoco and coverage maps are not complete.
- Documentation and operations handoff: remains `87/100`; Phase 9 docs and operations evidence are not complete.
- Performance and reliability evidence: remains `84/100`; Phase 9 performance and distribution evidence are not complete.
